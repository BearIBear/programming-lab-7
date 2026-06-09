package lab6.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;

import lab6.util.*;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import lab6.server.commands.Add;
import lab6.server.commands.AddIfMax;
import lab6.server.commands.Clear;
import lab6.server.commands.Command;
import lab6.server.commands.CountLessThanDescription;
import lab6.server.commands.FilterContainsName;
import lab6.server.commands.FilterGreaterThanGenre;
import lab6.server.commands.Head;
import lab6.server.commands.Help;
import lab6.server.commands.Info;
import lab6.server.commands.RemoveById;
import lab6.server.commands.Save;
import lab6.server.commands.Script;
import lab6.server.commands.Show;
import lab6.server.commands.Update;
import lab6.server.managers.CollectionManager;
import lab6.server.managers.CommandManager;
import lab6.server.managers.FileManager;

public class MainServer {
    private static final HashMap<UUID, ArrayList<Packet>> userPackets = new HashMap<>();
    private static final Logger log = LogManager.getLogger(MainServer.class);
    public static void main(String[] args) {
        String fileName = System.getenv("INPUT_FILENAME");
        if (fileName == null || fileName.isBlank()) {
            fileName = "data/Data.json";
            log.warn("Env variable INPUT_FILENAME doesn't exist!");
            log.warn("Defaulting to file: " + fileName);
        }

        String[] fileNames = null;
        try {
            Stream<Path> pathStream = Files.list(Paths.get("./data/scripts/"));
            fileNames = pathStream.filter(Files::isRegularFile).map(Path::getFileName).map(Path::toString).toArray(String[]::new);
            pathStream.close();
        } catch (IOException e) {}
        
        CollectionManager collectionManager = new CollectionManager();
        FileManager fileManager = new FileManager(fileName);
        fileManager.load(collectionManager);

        CommandManager commandManager = new CommandManager();
        commandManager.register(new Help(collectionManager));
        commandManager.register(new Info(collectionManager));
        commandManager.register(new Add(collectionManager));
        commandManager.register(new Show(collectionManager));
        commandManager.register(new Save(collectionManager, fileManager));
        commandManager.register(new Clear(collectionManager));
        commandManager.register(new Update(collectionManager));
        commandManager.register(new RemoveById(collectionManager));
        commandManager.register(new Head(collectionManager));
        commandManager.register(new AddIfMax(collectionManager));
        commandManager.register(new CountLessThanDescription(collectionManager));
        commandManager.register(new Script(collectionManager));
        commandManager.register(new FilterContainsName(collectionManager));
        commandManager.register(new FilterGreaterThanGenre(collectionManager));
        Map<String, Command> commandsList = commandManager.getCommandsList();
        String[] commandNames = commandsList.keySet().toArray(String[]::new);

        try {
            Terminal terminal = TerminalBuilder.builder().system(true).nativeSignals(true).build();
            terminal.enterRawMode();
            NonBlockingReader reader = terminal.reader();
            terminal.handle(Terminal.Signal.INT, signal -> {
                log.info("Shutdown hook activator activated");
                System.exit(0); 
            });
            String serverCommand = "";
            char readCharacter = 0;
    
            try {
                Selector selector = Selector.open();
                DatagramChannel server = DatagramChannel.open();
                server.bind(new InetSocketAddress(37582));
                server.configureBlocking(false);
                server.register(selector, SelectionKey.OP_READ);
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                log.info("Server active, waiting for requests...");
                boolean working = true;

                StringBuilder stringBuilder = new StringBuilder();
    
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    log.info("Shutdown hook activated");
                    commandsList.get("save").run(new String[1], null);
                    System.out.println("Collection saved through shutdown hook");
                }));

                while (working) {
                    int selectionAmount = selector.selectNow();
                    if (selectionAmount > 0) {
                        SelectionKey selectedKey = selector.selectedKeys().toArray(SelectionKey[]::new)[0];
                        selector.selectedKeys().remove(selectedKey);
                        log.info("Request received, attempting to work with it...");
                        SocketAddress clientAddress = server.receive(buffer);
                        buffer.flip();
                        byte[] receivedData = new byte[buffer.remaining()];
                        buffer.get(receivedData);
                        buffer.clear();
        
                        Packet receivedPacket = SerializationUtils.deserialize(receivedData);
                        UUID receivedUUID = receivedPacket.getClientUUID();
                        if (receivedPacket.isConnectionDefining()) {
                            if (userPackets.containsKey(receivedUUID)) {
                                log.info("Client disconnected with UUID: " + receivedUUID);
                            } else {
                                userPackets.put(receivedUUID, new ArrayList<>());
                                log.info("Client connected with UUID: " + receivedUUID);
        
                                ArrayList<Packet> packetsToSend = (ArrayList<Packet>) Packet.packObject(receivedUUID, commandNames);
                                Packet.serverSendPackets(server, packetsToSend, clientAddress);
        
                                packetsToSend = (ArrayList<Packet>) Packet.packObject(receivedUUID, fileNames);
                                Packet.serverSendPackets(server, packetsToSend, clientAddress);
                            }
                        } else {
                            ArrayList<Packet> packets = userPackets.get(receivedUUID); 
                            packets.add(receivedPacket);
                            log.info("Packet received from client with UUID: " + receivedUUID);
        
                            if (packets.size() == receivedPacket.getPacketsAmount()) {
                                CommandPayload commandPayload = (CommandPayload) Packet.restoreObject(packets);
                                CommandResult result;
                                if (!commandPayload.getCommandName().equals("save")) {
                                    result = commandsList.get(commandPayload.getCommandName()).run(commandPayload.getArgs(), commandPayload.getBand());
                                    commandManager.clearScriptFiles();
                                    commandManager.setRecursionForcedExit(false);
                                } else {
                                    result = new CommandResult(true, "Ты чё");
                                }
                                ArrayList<Packet> packetsToSend = (ArrayList<Packet>) Packet.packObject(receivedUUID, result);
                                Packet.serverSendPackets(server, packetsToSend, clientAddress);
                                userPackets.put(receivedUUID, new ArrayList<>());
                                log.info("Command " + commandPayload.getCommandName() + " executed from client with UUID: " + receivedUUID);
                            }
                        }
                    }

                    int codeCharacter;
                    try {
                        codeCharacter = reader.read(10L);
                    } catch (Exception e) {
                        codeCharacter = -1;
                    }
                    if (codeCharacter >= 0) {
                        readCharacter = (char) codeCharacter;
                        if (readCharacter == '\r' || readCharacter == '\n' || codeCharacter == 10 || codeCharacter == 13 || codeCharacter == 3) {
                            System.out.print("\r\n");
                            serverCommand = stringBuilder.toString().trim();
                            stringBuilder.setLength(0);
                            if (serverCommand.equals("exit") || codeCharacter == 3) {
                                log.info("You are absolutely right! We shouldn't just save the collection — we should shut down the server");
                                System.exit(0);
                                working = false;
                            } else if (serverCommand.equals("save")) {
                                log.info("Collection saved");
                                commandsList.get("save").run(new String[1], null);
                            }
                        } else if (codeCharacter == 8 || codeCharacter == 127) {
                            if (stringBuilder.length() != 0) {
                                System.out.print(readCharacter);
                                System.out.print(" ");
                                System.out.print(readCharacter);
                                stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
                            }
                        } else if (codeCharacter == 3) {
                            System.exit(0);
                        } else {
                            stringBuilder.append(readCharacter);
                            System.out.print(readCharacter);
                        }
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

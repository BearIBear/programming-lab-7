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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;

import lab6.util.*;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import lab6.server.commands.*;
import lab6.server.managers.*;

/**
 * Главный класс сервера, реализующий многопоточную обработку запросов
 *
 * @author Михаил
 */
public class MainServer {
    private static final HashMap<UUID, ArrayList<Packet>> userPackets = new HashMap<>();
    private static final Logger log = LogManager.getLogger(MainServer.class);

    // ThreadLocal для хранения имени текущего пользователя в потоке выполнения
    // команды
    public static final ThreadLocal<String> currentUser = new ThreadLocal<>();

    public static void main(String[] args) {
        String[] fileNames = null;
        try {
            Stream<Path> pathStream = Files.list(Paths.get("./data/scripts/"));
            fileNames = pathStream.filter(Files::isRegularFile).map(Path::getFileName).map(Path::toString)
                    .toArray(String[]::new);
            pathStream.close();
        } catch (IOException e) {
            fileNames = new String[0];
        }

        CollectionManager collectionManager = new CollectionManager();
        DatabaseManager databaseManager = new DatabaseManager();

        databaseManager.loadCollection(collectionManager);

        CommandManager commandManager = new CommandManager(databaseManager);
        commandManager.register(new Help(collectionManager));
        commandManager.register(new Info(collectionManager));
        commandManager.register(new Add(collectionManager));
        commandManager.register(new Show(collectionManager));
        commandManager.register(new Clear(collectionManager));
        commandManager.register(new Update(collectionManager));
        commandManager.register(new RemoveById(collectionManager));
        commandManager.register(new Head(collectionManager));
        commandManager.register(new AddIfMax(collectionManager));
        commandManager.register(new AddIfMin(collectionManager));
        commandManager.register(new CountLessThanDescription(collectionManager));
        commandManager.register(new Script(collectionManager));
        commandManager.register(new FilterContainsName(collectionManager));
        commandManager.register(new FilterGreaterThanGenre(collectionManager));

        Map<String, Command> commandsList = commandManager.getCommandsList();
        String[] commandNames = commandsList.keySet().toArray(String[]::new);

        ExecutorService readPool = Executors.newCachedThreadPool();
        ExecutorService processingPool = Executors.newCachedThreadPool();
        ExecutorService sendPool = Executors.newFixedThreadPool(10);

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
                log.info("Server active, waiting for requests on port 37582...");
                boolean working = true;

                StringBuilder stringBuilder = new StringBuilder();

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    log.info("Shutdown hook activated, shutting down pools...");
                    readPool.shutdown();
                    processingPool.shutdown();
                    sendPool.shutdown();
                }));

                final String[] finalFileNames = fileNames;

                while (working) {
                    int selectionAmount = selector.selectNow();
                    if (selectionAmount > 0) {
                        SelectionKey selectedKey = selector.selectedKeys().toArray(SelectionKey[]::new)[0];
                        selector.selectedKeys().remove(selectedKey);
                        if (selectedKey.isReadable()) {
                            // Попросим поток прочитать пакет
                            readPool.submit(() -> {
                                try {
                                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                                    SocketAddress clientAddress = server.receive(buffer);
                                    if (clientAddress != null) {
                                        buffer.flip();
                                        byte[] receivedData = new byte[buffer.remaining()];
                                        buffer.get(receivedData);

                                        // Попросим поток обработать полученный пакет
                                        processingPool.submit(() -> {
                                            try {
                                                Packet receivedPacket = SerializationUtils.deserialize(receivedData);
                                                UUID receivedUUID = receivedPacket.getClientUUID();

                                                if (receivedPacket.isConnectionDefining()) {
                                                    synchronized (userPackets) {
                                                        if (userPackets.containsKey(receivedUUID)) {
                                                            log.info("Client disconnected with UUID: " + receivedUUID);
                                                            userPackets.remove(receivedUUID);
                                                        } else {
                                                            userPackets.put(receivedUUID, new ArrayList<>());
                                                            log.info("Client connected with UUID: " + receivedUUID);

                                                            ArrayList<Packet> packetsToSend = (ArrayList<Packet>) Packet
                                                                    .packObject(receivedUUID, commandNames);
                                                            ArrayList<Packet> filesPackets = (ArrayList<Packet>) Packet
                                                                    .packObject(receivedUUID, finalFileNames);

                                                            sendPool.submit(() -> {
                                                                try {
                                                                    Packet.serverSendPackets(server, packetsToSend,
                                                                            clientAddress);
                                                                    Packet.serverSendPackets(server, filesPackets,
                                                                            clientAddress);
                                                                } catch (IOException e) {
                                                                    log.error("Failed to send init packet to client: "
                                                                            + e.getMessage());
                                                                }
                                                            });
                                                        }
                                                    }
                                                } else {
                                                    ArrayList<Packet> packets;
                                                    boolean reassembled = false;
                                                    synchronized (userPackets) {
                                                        packets = userPackets.get(receivedUUID);
                                                        if (packets == null) {
                                                            packets = new ArrayList<>();
                                                            userPackets.put(receivedUUID, packets);
                                                        }
                                                        packets.add(receivedPacket);
                                                        if (packets.size() == receivedPacket.getPacketsAmount()) {
                                                            reassembled = true;
                                                            userPackets.put(receivedUUID, new ArrayList<>());
                                                        }
                                                    }

                                                    if (reassembled) {
                                                        CommandPayload commandPayload = (CommandPayload) Packet
                                                                .restoreObject(packets);
                                                        CommandResult result;

                                                        String cmdName = commandPayload.getCommandName();
                                                        String username = commandPayload.getUsername();
                                                        String password = commandPayload.getPassword();

                                                        // Авторизация и регистрация
                                                        if (cmdName.equals("login") || cmdName.equals("register")) {
                                                            if (commandPayload.isRegister()) {
                                                                boolean success = databaseManager.registerUser(username,
                                                                        password);
                                                                if (success) {
                                                                    result = new CommandResult(true,
                                                                            "Регистрация успешна! Вход выполнен");
                                                                } else {
                                                                    result = new CommandResult(false,
                                                                            "Пользователь с таким именем уже существует");
                                                                }
                                                            } else {
                                                                boolean success = databaseManager.validateUser(username,
                                                                        password);
                                                                if (success) {
                                                                    result = new CommandResult(true,
                                                                            "Вход выполнен успешно!");
                                                                } else {
                                                                    result = new CommandResult(false,
                                                                            "Неверное имя пользователя или пароль");
                                                                }
                                                            }
                                                        } else {
                                                            // Проверка авторизации для дефолтных команд
                                                            boolean authorized = databaseManager.validateUser(username,
                                                                    password);
                                                            if (!authorized) {
                                                                result = new CommandResult(false,
                                                                        "Ошибка авторизации! Перезапустите клиент");
                                                            } else {
                                                                currentUser.set(username);
                                                                boolean isWrite = cmdName.equals("add") ||
                                                                        cmdName.equals("add_if_max") ||
                                                                        cmdName.equals("add_if_min") ||
                                                                        cmdName.equals("clear") ||
                                                                        cmdName.equals("remove_by_id") ||
                                                                        cmdName.equals("update");

                                                                if (isWrite) {
                                                                    collectionManager.getLock().writeLock().lock();
                                                                } else {
                                                                    collectionManager.getLock().readLock().lock();
                                                                }

                                                                try {
                                                                    if (commandsList.containsKey(cmdName)) {
                                                                        result = commandsList.get(cmdName).run(
                                                                                commandPayload.getArgs(),
                                                                                commandPayload.getBand());
                                                                        commandManager.clearScriptFiles();
                                                                        commandManager.setRecursionForcedExit(false);
                                                                    } else {
                                                                        result = new CommandResult(false,
                                                                                "Ты чё нам тут отправляешь поддельные комманды?");
                                                                    }
                                                                } finally { // Иначе есть шанс, что замок не отпустится
                                                                    if (isWrite) {
                                                                        collectionManager.getLock().writeLock()
                                                                                .unlock();
                                                                    } else {
                                                                        collectionManager.getLock().readLock().unlock();
                                                                    }
                                                                    currentUser.remove();
                                                                }
                                                            }
                                                        }

                                                        // Попросим один из потоков отправить ответ
                                                        final CommandResult finalResult = result;
                                                        ArrayList<Packet> packetsToSend = (ArrayList<Packet>) Packet
                                                                .packObject(receivedUUID, finalResult);
                                                        sendPool.submit(() -> {
                                                            try {
                                                                Packet.serverSendPackets(server, packetsToSend,
                                                                        clientAddress);
                                                            } catch (IOException e) {
                                                                log.error("Responce failure: " + e.getMessage());
                                                            }
                                                        });
                                                        log.info("Command " + cmdName + " executed from client: "
                                                                + receivedUUID);
                                                    }
                                                }
                                            } catch (Exception e) {
                                                log.error("Error executing client payload: " + e.getMessage(), e);
                                            }
                                        });
                                    }
                                } catch (IOException e) {
                                    log.error("Failed to read packet: " + e.getMessage());
                                }
                            });
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
                        if (readCharacter == '\r' || readCharacter == '\n' || codeCharacter == 10 || codeCharacter == 13
                                || codeCharacter == 3) {
                            System.out.print("\r\n");
                            serverCommand = stringBuilder.toString().trim();
                            stringBuilder.setLength(0);
                            if (serverCommand.equals("exit") || codeCharacter == 3) {
                                log.info(
                                        "You are absolutely right! We shouldn't just save the collection — we should shut down the server");
                                System.exit(0);
                                working = false;
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

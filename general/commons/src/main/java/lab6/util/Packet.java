package lab6.util;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import lab6.models.*;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SerializationUtils;

// HEADER   DATA    HEADER   DATA
public class Packet implements Serializable, Comparable<Packet> {
    public byte[] serializedClientUUID;
    public byte packetsAmount;
    public byte currentPacket; // Пакеты нумеровать будем как: [0;4]
    public short readableDataLength;
    public byte[] data;
    private static final long serialVersionUID = 1L;

    private static final int MAX_PACKET_SIZE = 1024;
    public static final int DATA_LENGTH;

    static {
        Packet test = new Packet(UUID.randomUUID(), 1, 0, new byte[0]);
        int overhead = SerializationUtils.serialize(test).length;
        DATA_LENGTH = MAX_PACKET_SIZE - overhead;
    }



    public Packet(UUID clientUUID, byte packetsAmount, byte currentPacket, byte[] data) {
        this.serializedClientUUID = SerializationUtils.serialize(clientUUID);
        this.packetsAmount = packetsAmount;
        this.currentPacket = currentPacket;
        if (data != null) {
            this.readableDataLength = (short) data.length;
            if (data.length < DATA_LENGTH) {
                this.data = Arrays.copyOf(data, DATA_LENGTH);
            } else {
                this.data = data;
            }
        } else {
            this.readableDataLength = 0;
            this.data = new byte[DATA_LENGTH];
        }
    }


    public Packet(UUID clientUUID, int packetsAmount, int currentPacket, byte[] data) {
        this.serializedClientUUID = SerializationUtils.serialize(clientUUID);
        this.packetsAmount = (byte) packetsAmount;
        this.currentPacket = (byte) currentPacket;
        if (data != null) {
            this.readableDataLength = (short) data.length;
            if (data.length < DATA_LENGTH) {
                this.data = Arrays.copyOf(data, DATA_LENGTH);
            } else {
                this.data = data;
            }
        } else {
            this.readableDataLength = 0;
            this.data = new byte[DATA_LENGTH];
        }
    }

    public static List<Packet> packObject(UUID clientUUID, Serializable o) {
        byte[] serializedObject = SerializationUtils.serialize(o);
        int capacity = DATA_LENGTH;
        byte packetsCount = (byte) Math.ceil((double) serializedObject.length / capacity); 
        ArrayList<Packet> packets = new ArrayList<>();
        for (int i = 0; i < packetsCount; i++) {
            packets.add(new Packet(clientUUID, packetsCount, i,
                Arrays.copyOfRange(serializedObject, i * capacity, Math.min((i + 1) * capacity, serializedObject.length))));
        }
        return packets;
    }

    public static Serializable restoreObject(ArrayList<Packet> packets) {
        packets.sort(null);
        byte[] data = new byte[0];
        for (Packet packet : packets) {
            data = ArrayUtils.addAll(data, packet.getActualData());
        }
        return SerializationUtils.deserialize(data);
    }

    public static void serverSendPackets(DatagramChannel server, ArrayList<Packet> packets, SocketAddress address) throws IOException {
        ByteBuffer sendBuffer;
        for (Packet packet : packets) {
            byte[] serializedPacket = SerializationUtils.serialize(packet);
            sendBuffer = ByteBuffer.wrap(serializedPacket);
            server.send(sendBuffer, address);
        }
    }

    public static void clientSendPackets(DatagramSocket client, ArrayList<Packet> packets, InetAddress address, int port) throws IOException {
        DatagramPacket sendDatagramPacket;
        for (Packet packet : packets) {
            byte[] serializedPacket = SerializationUtils.serialize(packet);
            sendDatagramPacket = new DatagramPacket(serializedPacket, 1024, address, port);
            client.send(sendDatagramPacket);
        }
    }


    public static void main(String[] args) {
        // Packet bear = new Packet(UUID.randomUUID(), 2, 0, SerializationUtils.serialize(Color.BLACK));
        // Packet koala = new Packet(UUID.randomUUID(), 2, 1, SerializationUtils.serialize(Color.YELLOW));
        // System.out.println(SerializationUtils.serialize(bear).length);
        // System.out.println(SerializationUtils.serialize(koala).length);
        UUID clientUUID = UUID.randomUUID();
        MusicBand bears = new MusicBand("The Lighthouse Madness", new Coordinates((long) 50, 100), 200,
        500, "Doodle let me go, doodle let me go, doodle let me go", MusicGenre.MATH_ROCK, new Person("Thomas Wake", LocalDate.now(), Color.BROWN));
        System.out.println("Expected UUID: " + clientUUID);
        System.out.println("MusicBand size: " + SerializationUtils.serialize(bears).length);
        ArrayList<Packet> packets = (ArrayList<Packet>) Packet.packObject(clientUUID, bears);
        System.out.println(packets);
        MusicBand bearsRestored = (MusicBand) Packet.restoreObject(packets);
        System.out.println(bearsRestored); 
    }

    public byte[] getSerializedClientUUID() {
        return serializedClientUUID;
    }

    public UUID getClientUUID() {
        return SerializationUtils.deserialize(serializedClientUUID);
    }

    public byte getPacketsAmount() {
        return packetsAmount;
    }

    public byte getCurrentPacket() {
        return currentPacket;
    }

    public short getReadableDataLength() {
        return readableDataLength;
    }

    public byte[] getData() {
        return data;
    }

    public byte[] getActualData() {
        return Arrays.copyOfRange(this.data, 0, this.readableDataLength);
    }

    public boolean isSingle() {
        return packetsAmount == 1;
    }

    public boolean isConnectionDefining() {
        return readableDataLength == 0;
    }

    @Override
    public int compareTo(Packet o) {
        return Byte.compare(this.currentPacket, o.currentPacket);
    }

    @Override
    public String toString() {
        return "Packet [packetsAmount=" + packetsAmount + ", currentPacket=" + currentPacket + ", readableDataLength="
                + readableDataLength + "]";
    }
}

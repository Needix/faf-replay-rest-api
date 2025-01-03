package de.needix.games.faf.replay.analyser.parser;

import de.needix.games.faf.replay.api.entities.replay.Replay;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ReplayParser {
    private final ByteArrayInputStream buffer;
    private final Integer bufferSize;

    private final ReplayHeader replayHeader;
    private final ReplayBody replayBody;
    private final Replay replayToFill;

    public ReplayParser(byte[] inputData, Replay replayToFill, Consumer<Command> commandConsumer) {
        this.buffer = new ByteArrayInputStream(inputData);
        this.bufferSize = inputData.length;

        this.replayToFill = replayToFill;

        this.replayHeader = new ReplayHeader(this, replayToFill);
        this.replayBody = new ReplayBody(this, replayToFill);
        this.replayBody.parse(commandConsumer);
    }

    public String readString() {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        while (true) {
            int byteData = buffer.read();
            if (byteData == -1 || byteData == 0) {
                break;
            }
            result.write(byteData);
        }
        return result.toString(StandardCharsets.UTF_8);
    }

    public int readInt() {
        return readNumber(4).getInt();
    }

    private ByteBuffer readNumber(int size) {
        byte[] data = read(size);
        return ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    }

    public byte[] read(int size) {
        if (size == 0) {
            return new byte[0];
        }

        byte[] data = new byte[size];
        int bytesRead;
        try {
            bytesRead = buffer.read(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (bytesRead != size) {
            throw new RuntimeException("Unexpected end of stream");
        }
        return data;
    }

    public int readUnsignedInt() {
        return readNumber(4).getInt();
    }

    public short readShort() {
        return readNumber(2).getShort();
    }

    public float readFloat() {
        return readNumber(4).getFloat();
    }

    public int readByte() {
        return buffer.read() & 0xFF;
    }

    public boolean readBool() {
        int readByte = readByte();
        return readByte != 0;
    }

    public void readNil() {
        buffer.read();
    }

    public Map<Object, Object> readDict() {
        Map<Object, Object> result = new HashMap<>();
        while (true) {
            int type = readByte();
            if (type == DataType.END) {
                break;
            }
            Object key = readLua(type);
            Object value = readLua(null);
            result.put(key, value);
        }
        return result;
    }

    public Object readLua(Integer type) {
        if (type == null) {
            type = readByte();
        }

        return switch (type) {
            case DataType.NUMBER -> readFloat();
            case DataType.STRING -> readString();
            case DataType.NIL -> {
                readNil();
                yield null;
            }
            case DataType.BOOL -> readBool();
            case DataType.TABLE -> readDict();
            default -> throw new IllegalArgumentException("Unknown data type: " + type);
        };
    }

    public int offset() {
        return bufferSize - buffer.available();
    }

    public int size() {
        return bufferSize;
    }

    public static class DataType {
        public static final int END = 5;
        public static final int NUMBER = 0;
        public static final int STRING = 1;
        public static final int NIL = 2;
        public static final int BOOL = 3;
        public static final int TABLE = 4;
    }
}

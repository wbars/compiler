package me.wbars.compiler.generator;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class NumberToByteConverter {
    private NumberToByteConverter() {}
    public static List<Byte> convert(Integer value, int size) {
        return getBytes(size, ByteBuffer.allocate(4).putInt(value).array());
    }

    public static List<Byte> convert(Long value, int size) {
        return getBytes(size, ByteBuffer.allocate(8).putLong(value).array());
    }

    public static List<Byte> convert(Double value, int size) {
        return getBytes(size, ByteBuffer.allocate(8).putDouble(value).array());
    }

    private static List<Byte> getBytes(int size, byte[] bytes) {
        List<Byte> res = new ArrayList<>();
        for (byte b : bytes) {
            res.add(b);
        }
        return res.subList(bytes.length - size, res.size());
    }
}

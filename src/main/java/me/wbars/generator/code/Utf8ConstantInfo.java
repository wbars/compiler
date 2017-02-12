package me.wbars.generator.code;

import me.wbars.generator.IntegerToByteConverter;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Utf8ConstantInfo implements ConstantInfo {
    private final int index;
    private final String value;

    public Utf8ConstantInfo(String value, int index) {
        this.index = index;
        this.value = value;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public int getTag() {
        return 1;
    }

    @Override
    public String getRawValue() {
        return value;
    }

    @Override
    public List<Byte> toBytes() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(out);
        try {
            outputStream.writeUTF(value);
        } catch (IOException e) {
            return null;
        }

        byte[] bytes = out.toByteArray();
        List<Byte> result = new ArrayList<>();
        //todo what meaning of 2 first bytes?
        result.addAll(IntegerToByteConverter.convert(bytes.length - 2, 2));
        for (int i = 2; i < bytes.length; i++) {
            byte b = bytes[i];
            result.add(b);
        }
        return result;
    }
}

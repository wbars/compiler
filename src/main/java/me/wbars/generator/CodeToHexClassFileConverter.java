package me.wbars.generator;

import me.wbars.generator.code.ConstantInfo;
import me.wbars.generator.code.GeneratedCode;
import me.wbars.semantic.models.types.TypeRegistry;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

/**
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html">Jvm class file format</a>
 */
public class CodeToHexClassFileConverter {
    private final int magic = 0xCAFEBABE;
    private final int minorVersion = 0;
    private final int majorVersion = 52;
    private final int accessFlagSuper = 0x0020;
    private final int accessFlagPublic = 0x0001;
    private final int superClass = 0;
    private final int methodsCount = 2;
    private final GeneratedCode code;

    private static final String SOURCE_FILENAME = "Main.java";
    private static final String VOID_TYPE = "()V";
    private static final String THIS_CLASS = "Main";
    private static final String SOURCE_FILE_TAG = "SourceFile";
    private static final String CODE_FIELD = "Code";
    private static final String MAIN_TYPE_DESCRIPTOR = "([Ljava/lang/String;)V";
    private final String OBJECT_CLASS = "java/lang/Object";
    private final int INTERFACES_COUNT = 0;
    private final int FIELDS_COUNT = 0;
    private final int METHODS_COUNT = 2;
    private final int ATTRIBUTES_COUNT = 1;
    private final List<Byte> INIT_METHOD;
    private final List<Byte> MAIN_METHOD;
    private final List<Byte> SOURCE_FILE_ATTRIBUTE;
    private final int THIS_CLASS_INDEX;
    private final int OBJECT_CLASS_INDEX;
    private final int CLASS_FLAG_SUPER = 0;
    private final String BOOTSTRAP_INIT_METHOD = "<init>";
    private final int BOOTSTRAP_ATTRIBUTES_COUNT = 1;

    private final List<Byte> ONE_2_BYTES = IntegerToByteConverter.convert(1, 2);
    private final List<Byte> ZERO_2_BYTES = IntegerToByteConverter.convert(0, 2);

    private final List<Byte> BOOTSTRAP_METHOD_MAX_STACK = ONE_2_BYTES;
    private final List<Byte> BOOTSTRAP_METHOD_MAX_LOCALS = ONE_2_BYTES;
    private final List<Byte> BOOTSTRAP_METHOD_EXCEPTIONS_COUNT = ZERO_2_BYTES;
    private final List<Byte> BOOTSTRAP_METHOD_ATTRIBUTES_COUNT = ZERO_2_BYTES;
    private final List<Byte> METHOD_PUBLIC_STATIC_MASK = IntegerToByteConverter.convert(9, 2);
    private final String MAIN_METHOD_NAME = "main";
    private final List<Byte> MAIN_METHOD_ATTRIBUTES_COUNT = ONE_2_BYTES;
    private final List<Byte> MAIN_METHOD_EXCEPTIONS_COUNT = ZERO_2_BYTES;
    private final List<Byte> SOURCE_FILE_ATTRIBUTE_ARGUMENT_SIZE = IntegerToByteConverter.convert(2, 4);
    private List<Byte> MAIN_METHOD_BODY_ATTRIBUTES_COUNT = ZERO_2_BYTES;

    public CodeToHexClassFileConverter(GeneratedCode code) {
        this.code = code;
        THIS_CLASS_INDEX = code.getConstantPool().getClass(THIS_CLASS);
        OBJECT_CLASS_INDEX = code.getConstantPool().getClass(OBJECT_CLASS);
        INIT_METHOD = mainBootstrapMethod();
        MAIN_METHOD = mainMethod();
        SOURCE_FILE_ATTRIBUTE = sourceFileAttribute();
    }

    private List<Byte> flatten(List<List<Byte>> values) {
        return values.stream()
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public List<Byte> convert() {
        return flatten(asList(
                IntegerToByteConverter.convert(magic, 4), //magic
                IntegerToByteConverter.convert(minorVersion, 2),
                IntegerToByteConverter.convert(majorVersion, 2),
                constantPoolToBytes(),
                IntegerToByteConverter.convert(accessFlagSuper, 2),
                IntegerToByteConverter.convert(THIS_CLASS_INDEX, 2),
                IntegerToByteConverter.convert(OBJECT_CLASS_INDEX, 2),
                IntegerToByteConverter.convert(INTERFACES_COUNT, 2),
                IntegerToByteConverter.convert(FIELDS_COUNT, 2),

                IntegerToByteConverter.convert(METHODS_COUNT, 2),
                INIT_METHOD,
                MAIN_METHOD,

                IntegerToByteConverter.convert(ATTRIBUTES_COUNT, 2),
                SOURCE_FILE_ATTRIBUTE)
        );
    }

    private List<Byte> mainBootstrapMethod() {
        List<Byte> codeLinesBytes = generateBytecode(asList(
                JvmBytecodeCommandFactory.loadRegister(0, TypeRegistry.STRING),
                JvmBytecodeCommandFactory.invokeSpecial(code.getConstantPool().getFieldOrMethodIndex("java/lang/Object.<init>", "()V")),
                JvmBytecodeCommandFactory.returnCommand()
        ));
        List<Byte> bodyBytes = flatten(asList(
                BOOTSTRAP_METHOD_MAX_STACK,
                BOOTSTRAP_METHOD_MAX_LOCALS,
                IntegerToByteConverter.convert(codeLinesBytes.size(), 4),
                codeLinesBytes,
                BOOTSTRAP_METHOD_EXCEPTIONS_COUNT,
                BOOTSTRAP_METHOD_ATTRIBUTES_COUNT
        ));

        return flatten(asList(
                IntegerToByteConverter.convert(CLASS_FLAG_SUPER, 2),
                IntegerToByteConverter.convert(getStringConstant(BOOTSTRAP_INIT_METHOD), 2),
                IntegerToByteConverter.convert(getStringConstant(VOID_TYPE), 2),
                IntegerToByteConverter.convert(BOOTSTRAP_ATTRIBUTES_COUNT, 2),
                IntegerToByteConverter.convert(getStringConstant(CODE_FIELD), 2),
                IntegerToByteConverter.convert(bodyBytes.size(), 4),
                bodyBytes
        ));
    }

    private List<Byte> generateBytecode(List<CodeLine> codeLines) {
        return codeLines.stream()
                .flatMap(l -> generateBytecode(l).stream())
                .collect(Collectors.toList());
    }

    private List<Byte> mainMethod() {
        List<Byte> codeLines = generateBytecode(code.getLines());
        List<Byte> codeBody = flatten(asList(
                IntegerToByteConverter.convert(code.getMaxStack(), 2),
                IntegerToByteConverter.convert(code.getMaxLocals(), 2),
                IntegerToByteConverter.convert(codeLines.size(), 4),
                codeLines,
                MAIN_METHOD_EXCEPTIONS_COUNT,
                MAIN_METHOD_BODY_ATTRIBUTES_COUNT

        ));
        return flatten(asList(
                METHOD_PUBLIC_STATIC_MASK,
                IntegerToByteConverter.convert(getStringConstant(MAIN_METHOD_NAME), 2),
                IntegerToByteConverter.convert(getStringConstant(MAIN_TYPE_DESCRIPTOR), 2),
                MAIN_METHOD_ATTRIBUTES_COUNT,
                IntegerToByteConverter.convert(getStringConstant(CODE_FIELD), 2),
                IntegerToByteConverter.convert(codeBody.size(), 4),
                codeBody
        ));
    }

    private int getStringConstant(String value) {
        return code.getConstantPool().getConstantIndex(value, TypeRegistry.UTF8);
    }

    private List<Byte> sourceFileAttribute() {
        return flatten(asList(
                IntegerToByteConverter.convert(getStringConstant(SOURCE_FILE_TAG), 2),
                SOURCE_FILE_ATTRIBUTE_ARGUMENT_SIZE,
                IntegerToByteConverter.convert(getStringConstant(SOURCE_FILENAME), 2)
        ));
    }

    private List<Byte> generateBytecode(CodeLine codeLine) {
        return flatten(asList(
                IntegerToByteConverter.convert(codeLine.getCommand().getCode(), 2),
                codeLine.getArgument() != null ? IntegerToByteConverter.convert(codeLine.getArgument(), codeLine.getCommand().getArgumentsSize()) : null
        ));
    }

    private List<Byte> constantPoolToBytes() {
        return flatten(asList(
                IntegerToByteConverter.convert(code.getConstantPool().size(), 2),
                constantsBytes()
        ));
    }

    private List<Byte> constantsBytes() {
        return code.getConstantPool().getConstants().stream()
                .skip(1)
                .flatMap(c -> constantInfoToBytes(c).stream())
                .collect(Collectors.toList());
    }

    private List<Byte> constantInfoToBytes(ConstantInfo constantInfo) {
        return flatten(asList(
                IntegerToByteConverter.convert(constantInfo.getTag(), 1),
                constantInfo.toBytes()
        ));
    }

    public static void toFile(GeneratedCode code, String path) throws IOException {
        FileOutputStream fos = new FileOutputStream(path);
        new CodeToHexClassFileConverter(code).convert().forEach(aByte -> {
            try {
                fos.write(aByte);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        fos.close();
    }
}

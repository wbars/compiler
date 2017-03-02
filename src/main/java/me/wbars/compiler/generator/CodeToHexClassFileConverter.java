package me.wbars.compiler.generator;

import me.wbars.compiler.generator.code.ConstantInfo;
import me.wbars.compiler.generator.code.GeneratedCode;
import me.wbars.compiler.semantic.models.types.TypeRegistry;
import me.wbars.compiler.utils.Registry;

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
    private final GeneratedCode code;

    private static final String VOID_TYPE = "()V";
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
    private final String BOOTSTRAP_INIT_METHOD = "<init>";

    private final List<Byte> ONE_2_BYTES = NumberToByteConverter.convert(1, 2);
    private final List<Byte> ZERO_2_BYTES = NumberToByteConverter.convert(0, 2);

    private final List<Byte> METHOD_PUBLIC_STATIC_MASK = NumberToByteConverter.convert(9, 2);
    private final String MAIN_METHOD_NAME = "main";
    private final List<Byte> MAIN_METHOD_ATTRIBUTES_COUNT = ONE_2_BYTES;
    private final List<Byte> MAIN_METHOD_EXCEPTIONS_COUNT = ZERO_2_BYTES;
    private final List<Byte> SOURCE_FILE_ATTRIBUTE_ARGUMENT_SIZE = NumberToByteConverter.convert(2, 4);
    private List<Byte> MAIN_METHOD_BODY_ATTRIBUTES_COUNT = ZERO_2_BYTES;

    public CodeToHexClassFileConverter(GeneratedCode code) {
        this.code = code;
        THIS_CLASS_INDEX = code.getConstantPool().getClass(code.getClassName());
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
                NumberToByteConverter.convert(magic, 4), //magic
                NumberToByteConverter.convert(minorVersion, 2),
                NumberToByteConverter.convert(majorVersion, 2),
                constantPoolToBytes(),
                NumberToByteConverter.convert(accessFlagSuper, 2),
                NumberToByteConverter.convert(THIS_CLASS_INDEX, 2),
                NumberToByteConverter.convert(OBJECT_CLASS_INDEX, 2),
                NumberToByteConverter.convert(INTERFACES_COUNT, 2),
                NumberToByteConverter.convert(FIELDS_COUNT, 2),

                NumberToByteConverter.convert(METHODS_COUNT + code.getConstantPool().customMethodsCount(), 2),
                INIT_METHOD,
                MAIN_METHOD,
                customMethods(),

                NumberToByteConverter.convert(ATTRIBUTES_COUNT, 2),
                SOURCE_FILE_ATTRIBUTE)
        );
    }

    private List<Byte> customMethods() {
        Registry<GeneratedCode> registry = code.getConstantPool().getCustomFunctionCodeRegistry();
        return registry.keys().stream()
                .flatMap(name -> createMethod(name,
                        code.getConstantPool().getCustomFunctionDescriptorRegistry().lookup(name),
                        registry.lookup(name)).stream()
                )
                .collect(Collectors.toList());
    }

    private List<Byte> createMethod(String name, String descriptor, GeneratedCode code) {
        return createMethod(name, descriptor, generateBytecode(code.getLines()), code.getMaxStack(), code.getMaxLocals());
    }

    private List<Byte> createMethod(String name, String descriptor, List<Byte> codeLines, int maxStack, int maxLocals) {
        List<Byte> codeBody = flatten(asList(
                NumberToByteConverter.convert(maxStack, 2),
                NumberToByteConverter.convert(maxLocals, 2),
                NumberToByteConverter.convert(codeLines.size(), 4),
                codeLines,
                MAIN_METHOD_BODY_ATTRIBUTES_COUNT,
                ZERO_2_BYTES

        ));
        return flatten(asList(
                METHOD_PUBLIC_STATIC_MASK,
                NumberToByteConverter.convert(getStringConstant(name), 2),
                NumberToByteConverter.convert(getStringConstant(descriptor), 2),
                MAIN_METHOD_ATTRIBUTES_COUNT,
                NumberToByteConverter.convert(getStringConstant(CODE_FIELD), 2),
                NumberToByteConverter.convert(codeBody.size(), 4),
                codeBody
        ));
    }

    private List<Byte> mainBootstrapMethod() {
        List<Byte> codeLines = generateBytecode(asList(
                JvmBytecodeCommandFactory.loadRegister(0, TypeRegistry.STRING),
                JvmBytecodeCommandFactory.invokeSpecial(code.getConstantPool().getFieldOrMethodIndex("java/lang/Object.<init>", "()V")),
                JvmBytecodeCommandFactory.returnCommand()
        ));
        return createMethod(BOOTSTRAP_INIT_METHOD, VOID_TYPE, codeLines, 1, 1);
    }

    private List<Byte> generateBytecode(List<CodeLine> codeLines) {
        return codeLines.stream()
                .flatMap(l -> generateBytecode(l).stream())
                .collect(Collectors.toList());
    }

    private List<Byte> mainMethod() {
        return createMethod(MAIN_METHOD_NAME, MAIN_TYPE_DESCRIPTOR, code);
    }

    private int getStringConstant(String value) {
        return code.getConstantPool().getConstantIndex(value, TypeRegistry.UTF8);
    }

    private List<Byte> sourceFileAttribute() {
        return flatten(asList(
                NumberToByteConverter.convert(getStringConstant(SOURCE_FILE_TAG), 2),
                SOURCE_FILE_ATTRIBUTE_ARGUMENT_SIZE,
                NumberToByteConverter.convert(getStringConstant(code.getClassName() + ".java"), 2)
        ));
    }

    private List<Byte> generateBytecode(CodeLine codeLine) {
        return flatten(asList(
                NumberToByteConverter.convert(codeLine.getCommand().getCode(), 2),
                codeLine.getArgument() != null ? NumberToByteConverter.convert(codeLine.getArgument(), codeLine.getCommand().getArgumentsSize()) : null
        ));
    }

    private List<Byte> constantPoolToBytes() {
        return flatten(asList(
                NumberToByteConverter.convert(code.getConstantPool().size(), 2),
                constantsBytes()
        ));
    }

    private List<Byte> constantsBytes() {
        return code.getConstantPool().getConstants().stream()
                .filter(Objects::nonNull)
                .flatMap(c -> constantInfoToBytes(c).stream())
                .collect(Collectors.toList());
    }

    private List<Byte> constantInfoToBytes(ConstantInfo constantInfo) {
        return flatten(asList(
                NumberToByteConverter.convert(constantInfo.getTag(), 1),
                constantInfo.toBytes()
        ));
    }

    public static String toFile(GeneratedCode code) throws IOException {
        String className = code.getClassName() + ".class";
        FileOutputStream fos = new FileOutputStream(className);
        new CodeToHexClassFileConverter(code).convert().forEach(aByte -> {
            try {
                fos.write(aByte);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        fos.close();
        return code.getClassName();
    }
}

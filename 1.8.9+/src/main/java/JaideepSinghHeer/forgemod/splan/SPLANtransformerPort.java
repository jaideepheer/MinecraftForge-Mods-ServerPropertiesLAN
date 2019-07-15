package JaideepSinghHeer.forgemod.splan;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.HttpUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;


/**
 * This Class acts as the {@link IClassTransformer} for the ByteCode Editing during Compilation.
 * It is used on all the ByteCode files and is hence given a chance to Edit any ByteCode file.
 * Every ByteCode file before compilation undergoes Transformation by all the Classes derived from {@link IClassTransformer}
 * and registered as TransformerClasses by the {@link net.minecraftforge.fml.relauncher.IFMLLoadingPlugin} Classes.
 *
 * Hence, once registered, this Class can Edit the ByteCode of any Minecraft Class during Compilation.
 * <Probably> :)
 *
 */
@SideOnly(Side.CLIENT)
public class SPLANtransformerPort implements IClassTransformer {
    /**
     * This is the main and only function called during Compilation for ByteCode Manipulation.
     * It changes the {@link HttpUtil#getSuitableLanPort()} function.
     * It changes the 'serversocket = new ServerSocket(0)' statement to 'serversocket = new ServerSocket(SPLANtransformerPort.getPort())'.
     * Thus using the {@link SPLANtransformerPort#getPort()} function.
     *
     * @param name This is the obfuscated name of the class.
     * @param transformedName It is the canonical Class name of the Class whose ByteCode is currently being compiled.
     * @param basicClass It is the ByteArray which stores the Original ByteCode of this Class.
     *
     * @return It returns the ByteArray which should be Compiled instead, i.e. It returns the new edited ByteCode..!
     */
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        //System.out.println("--------------------> Trying Injection (PORT) !");
        if(transformedName.equals("net.minecraft.util.HttpUtil"))
        {
            // Found Target Class
            System.out.println("--------------------> Found Target Class ("+name+")("+transformedName+") !");
            // Create a new ClassNode to roam around the Class file.
            ClassNode node = new ClassNode();

            // Assign a ClassReader to the basicClass byte array, i.e. similar to asigning buffer to input.
            ClassReader reader = new ClassReader(basicClass);

            // Attach node to the ClassReader(similar to buffer)
            reader.accept(node, 0);

            // Make node Cycle through all the methods
            l1:
            for (MethodNode mn : node.methods) {
                // Find target method
                if (mn.name.equals("func_76181_a") || mn.name.equals("getSuitableLanPort")) {
                    // Reached Target Method
                    System.out.println("--------------------> Found Target Method ("+mn.name+") !");
                    InsnList insnList = mn.instructions;
                    // Iterate through method's instructions
                    for (int i = 0; i < insnList.size(); i++) {
                        AbstractInsnNode ain = insnList.get(i);
                        if (ain != null && ain.getType() == AbstractInsnNode.METHOD_INSN && ain.getOpcode() == Opcodes.INVOKESPECIAL && ((MethodInsnNode) ain).desc.equals("(I)V")) {
                            // ain2 is the instruction to store the fetched port number to a variable for returning.
                            AbstractInsnNode ain2 = ain.getPrevious();
                            if (ain2 != null &&  ain2.getOpcode() == Opcodes.ICONST_0) {
                                //Pretty positive we've found the right instruction that fetches and stores a port number for LAN.
                                System.out.println("--------------------> Found Target Instruction (ICONST_0) !");
                                insnList.remove(ain2);
                                insnList.insertBefore(ain, new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(getClass()), "getPort", "()I", false));
                                System.out.println("--------------------> Patched New Instruction (getPort) !");
                                break l1;
                            }
                        }
                    }

                }
            }

            // Create a ClassWriter and tell it to automatically manage Stack Size
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);

            // Attach ClassWriter to node(similar to output buffer)
            node.accept(writer);

            // Convert ClassWriter(output buffer) to byte array
            // And Return
            return writer.toByteArray();
        }
        else
        return basicClass;
    }

    /**
     * This function is made to be called in the {@link HttpUtil#getSuitableLanPort()} function to get a suitable lan port.
     *
     * The variable 'i' in the {@link HttpUtil#getSuitableLanPort()} function stores the value of our custom function
     * which is then returned back to be open as a LAN Port ...!
     *
     */
    public static int getPort()
    {
        System.out.println("Setting Port : "+ServerPropertiesLAN.instance.port);
        int port = ServerPropertiesLAN.instance.port;
        return port>0&&port<=65535?port:0;
    }
}

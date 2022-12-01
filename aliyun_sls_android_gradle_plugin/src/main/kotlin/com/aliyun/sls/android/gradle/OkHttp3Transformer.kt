package com.aliyun.sls.android.gradle

import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.didiglobal.booster.transform.asm.asIterable
import com.didiglobal.booster.transform.asm.className
import com.google.auto.service.AutoService
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

/**
 * @author gordon
 * @date 2022/12/1
 */
@AutoService(ClassTransformer::class)
class OkHttp3Transformer : ClassTransformer {
    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        println("[SLS](3) transform: ${klass.name}, className: ${klass.className}")

        if ("okhttp3/OkHttpClient" === klass.name) {
            return transformNewCall(context, klass)
        }

        if ("okhttp3/OkHttpClient\$Builder" === klass.name) {
            return transformBuilder(context, klass)
        }

        return klass


//        newCall?.instructions?.findAll(Opcodes.INVOKESTATIC)?.forEach {
//            println("sls transform, found instructions. start process")
//            newCall.instructions?.apply {
//                println("sls transform, start insert")
//                insertBefore(
//                    it,
//                    MethodInsnNode(Opcodes.INVOKESTATIC, OKHTTP3_UTILS_CLAZZ, OKHTTP3_METHOD, OKHTTP3_PARAMETERS, false)
//                )
//                insertBefore(it, VarInsnNode(Opcodes.ASTORE, 1))
//                insertBefore(it, VarInsnNode(Opcodes.ALOAD, 1))
//
//                println("sls transform, end insert")
//            }
//            println("sls transform, end process")
//        }

//        return klass
    }

    private fun transformNewCall(context: TransformContext, klass: ClassNode): ClassNode {
        println("[SLS] start transformNewCall: ${klass.className}")

        val newCall = klass.methods.find {
            "${it.name}${it.desc}" == "newCall(Lokhttp3/Request;)Lokhttp3/Call;"
        }

        val newInsn = InsnList()
        newInsn.add(LabelNode())
        newInsn.add(VarInsnNode(Opcodes.ALOAD, 1))
        newInsn.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC,
                OKHTTP3_UTILS_CLAZZ,
                OKHTTP3_METHOD,
                OKHTTP3_PARAMETERS,
                false
            )
        )
        newInsn.add(VarInsnNode(Opcodes.ASTORE, 1))

        newCall?.instructions?.insert(newInsn)

        println("[SLS] end transformNewCall: ${klass.className}")
        return klass
    }

    private fun transformBuilder(context: TransformContext, klass: ClassNode): ClassNode {
        println("[SLS] start transformBuilder: ${klass.className}")

        val builder = klass.methods.find {
            "${it.name}${it.desc}" == ".<init>()V" ||
                    "${it.name}${it.desc}" == ".<init>(Lokhttp3/OkHttpClient\$Builder;)V"
        }

        builder?.instructions?.asIterable()?.forEach {
            val opcode = it.opcode
            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) || (opcode == Opcodes.ATHROW)) {
                builder.instructions.apply {
                    val newInsn = InsnList()
                    newInsn.add(LabelNode())
                    newInsn.add(VarInsnNode(Opcodes.ALOAD, 1))
                    newInsn.add(MethodInsnNode(Opcodes.INVOKESTATIC, "", "", "", false))
                    insertBefore(it, newInsn)
                    println("[SLS] end transformBuilder: ${klass.className}")
                }


//                InsnList il = new InsnList();
//                il.add(new FieldInsnNode(GETSTATIC, cn.name, "timer", "J"));
//                il.add(new MethodInsnNode(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J"));
//                il.add(new InsnNode(LADD));
//                il.add(new FieldInsnNode(PUTSTATIC, cn.name, "timer", "J"));
//                instructions.insertBefore(item, il);
            }
        }
        return klass
    }


}

private const val OKHTTP3_UTILS_CLAZZ = "com/aliyun/sls/android/okhttp/OkHttp3Utils"
private const val OKHTTP3_METHOD = "newRequest"
private const val OKHTTP3_PARAMETERS = "(Lokhttp3/Request;)Lokhttp3/Request;"
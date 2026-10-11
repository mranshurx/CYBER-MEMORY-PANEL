package com.example.native

class NativeBridge {
    companion object {
        init {
            try {
                System.loadLibrary("shizukuinjector")
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    external fun getNativeStatus(): String
    external fun injectArtHook(targetMethodSig: String): Boolean
}

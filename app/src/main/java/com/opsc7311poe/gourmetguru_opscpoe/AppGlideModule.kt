package com.opsc7311poe.gourmetguru_opscpoe

import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule

@GlideModule
class MyAppGlideModule : AppGlideModule(){
    override fun isManifestParsingEnabled(): Boolean {
        return false
    }
}

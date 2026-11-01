package com.coder7575.sleepguard.enforcer

import android.content.Context
import expo.modules.core.interfaces.Package
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleProvider

class FocusEnforcerPackage : Package, ModuleProvider {
    override fun getModules(context: Context): List<Class<out Module>> {
        return listOf(FocusEnforcerModule::class.java)
    }
}

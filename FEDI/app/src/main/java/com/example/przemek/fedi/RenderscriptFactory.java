package com.example.przemek.fedi;

import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptC;

/**
 * Created by Przemek on 2017-07-08.
 */

public class RenderscriptFactory {
    public static ScriptC CreateScript(RenderScript rs, String filterType){
        if(filterType == null){
            return null;
        }
        if(filterType.equals("Jasność")){
            return new ScriptC_brightness(rs);
        }
        else if(filterType.equals("Kontrast")){
            return new ScriptC_contrast(rs);
        }
        return null;
    }
}

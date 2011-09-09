package org.torquebox.clojure.core;

import clojure.lang.Compiler;
import clojure.lang.RT;
import clojure.lang.Var;

public class ClojureUtil {

    public static Object loadAndInvoke(String script, String namespace, String function, Object arg1, Object arg2) throws Exception {    
        Compiler.loadFile( script );
        Var func = RT.var( namespace, function );
        return func.invoke( arg1, arg2 );
    }
}

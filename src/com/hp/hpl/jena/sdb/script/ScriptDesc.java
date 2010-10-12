/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.script;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

import com.hp.hpl.jena.sparql.util.graph.GraphUtils;

import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.assembler.AssemblerVocab;

/** Java description a script : the assmbler build one of these */ 

public class ScriptDesc
{
    List<CmdDesc> steps = new ArrayList<CmdDesc>() ;
    
    public static ScriptDesc read(String filename)
    {
        AssemblerVocab.init() ;
        Model m = FileManager.get().loadModel(filename) ;
        
        return worker(m) ;
    }
    
    public static void run(String filename)
    {
        ScriptDesc desc = ScriptDesc.read(filename) ;
//        System.out.println(desc) ;
//        try {
//            String cmd = desc.getCmd() ;
//            Class c = Class.forName(cmd) ;
//            Method m = c.getMethod("main", new Class[]{String[].class}) ;
//            m.invoke(null, new Object[]{desc.asStringArray()}) ;
//        } catch (Exception ex) { ex.printStackTrace(System.err) ; }
    }
    
    public void add(CmdDesc step) { steps.add(step) ; }
    public List<CmdDesc> getSteps() { return steps ; } // Temp
    
    private static ScriptDesc worker(Model m)
    {
        Resource r = GraphUtils.getResourceByType(m, ScriptVocab.ScriptType) ;
        if ( r == null )
            throw new SDBException("Can't find command line description") ;
        return (ScriptDesc)AssemblerBase.general.open(r) ;
    }
    
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
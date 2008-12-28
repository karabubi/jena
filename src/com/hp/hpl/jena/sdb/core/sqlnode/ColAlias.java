/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.sqlnode;

import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.shared.SDBInternalError;

public class ColAlias
{
    // "extends Pair" does not give nice names.
    private SqlColumn column ;
    private SqlColumn alias ;
    
    public ColAlias(SqlColumn column, SqlColumn alias)
    {
        this.column = column ;
        this.alias = alias ;
    }

    public SqlColumn getColumn()
    {
        return column ;
    }

    public SqlColumn getAlias()
    {
        return alias ;
    }
    
    public void check(String requiredName)
    {
        if ( getAlias() == null )
            return ;
        if ( getAlias().getTable() == null )
            return ;
        if ( ! getAlias().getTable().getAliasName().equals(requiredName) )
            throw new SDBInternalError("Alias name error: "+getColumn()+"/"+getAlias()+": required: "+requiredName) ;
    }
    
    @Override
    public String toString()
    {
        
        StringBuilder b = new StringBuilder() ;
        b.append("(") ;
        b.append(  (column == null) ? "??" : column ) ;
        b.append(",") ;
        b.append(  (alias == null) ? "??" : alias ) ;
        b.append(")") ;
        return b.toString() ; 
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.builders;

import java.util.HashMap ;
import java.util.Iterator ;
import java.util.Map ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.core.VarExprList ;
import com.hp.hpl.jena.sparql.expr.* ;
import com.hp.hpl.jena.sparql.expr.aggregate.Aggregator ;
import com.hp.hpl.jena.sparql.expr.aggregate.AggregatorFactory ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemList ;
import com.hp.hpl.jena.sparql.sse.Tags ;

public class BuilderExpr
{
    // Build an expr list when they may also be a single expression.
    // Because expressions are themselves lists, this requires ExprLists to be explicitly tagged
    
    // (exprlist ...)
    // ((expr1) (expr2))
    // (expr))
    public static ExprList buildExprOrExprList(Item item)
    {
        if ( item.isTagged(Tags.tagExprList) )
            return buildExprList(item) ;
        
        if ( listOfLists(item) )
            return buildExprListUntagged(item.getList(), 0) ;
        
        Expr expr = buildExpr(item) ; 
        ExprList exprList = new ExprList(expr) ;
        return exprList ;
    }

    private static boolean listOfLists(Item item)
    {
        // Atom
        if ( ! item.isList() ) return false ;
        // List of atom (inc tagged)
        if ( ! item.getList().car().isList() ) return false ;
        // List of lists
        return true ;
    }

    public static ExprList buildExprList(Item item)
    {
        if ( ! item.isTagged(Tags.tagExprList) )
            BuilderLib.broken(item, "Not tagged exprlist") ;
        
        ItemList list = item.getList() ;
        return buildExprListUntagged(list, 1) ;
    }
    
    private static ExprList buildExprListUntagged(Item item)
    {
        return buildExprListUntagged(item.getList(), 0) ;
    }

    private static ExprList buildExprListUntagged(ItemList list, int idx)
    {
        ExprList exprList = new ExprList() ;
        for ( int i = idx ; i < list.size() ; i++ )
        {
            Item item = list.get(i) ;
            exprList.add(buildExpr(item)) ;
        }
        return exprList ;
    }
    
    public static Expr buildExpr(Item item)
    {
        // If this (bob) is stateless, we can have one and use always.
        BuilderExpr bob = new BuilderExpr() ;
        return bob.buildItem(item) ;
    }

    public static VarExprList buildNamedExprOrExprList(Item item)
    {
        if ( ! item.isList() )
            BuilderLib.broken(item, "Not a var expr list") ;
        
        ItemList list = item.getList() ;
        
        if ( list.isEmpty() )
            return new VarExprList() ;
        
        if ( list.car().isList() )
            // List of lists
            return buildNamedExprList(list) ;
        // One item
        return buildNamedExpr(item) ;
    }
    
    public static VarExprList buildNamedExprList(ItemList list)
    {
        VarExprList x = new VarExprList() ;
        for ( Item item : list )
            buildNamedExpr(item, x) ;
        return x ;
    }
    
    public static VarExprList buildNamedExpr(Item item)
    {
        VarExprList varExprList = new VarExprList() ;
        buildNamedExpr(item, varExprList) ;
        return varExprList ;
    }
    
    private static void buildNamedExpr(Item item, VarExprList varExprList)
    {
        if ( item.isNode() )
        {
            Var v = BuilderNode.buildVar(item) ;
            varExprList.add(v) ;
            return ;
        }
        if ( !item.isList() || item.getList().size() != 2 )
            BuilderLib.broken(item, "Not a var or var/expression pair") ;

        ItemList list = item.getList() ;

        if ( list.size() == 1 )
        {
            Var v = BuilderNode.buildVar(list.car()) ;
            varExprList.add(v) ;
            return ;
        }
        
        if ( list.size() != 2 )
            BuilderLib.broken(list, "Not a var or var/expression pair") ;
        Var var = BuilderNode.buildVar(list.get(0)) ;
        Expr expr = BuilderExpr.buildExpr(list.get(1)) ;
        varExprList.add(var, expr) ;  
    }
    
    protected Map<String, Build> dispatch = new HashMap<String, Build>() ;
    public Expr buildItem(Item item)
    {
        Expr expr = null ;
    
        if ( item.isList() )
        {
            ItemList list = item.getList() ;
            
            if ( list.size() == 0 )
                BuilderLib.broken(item, "Empty list for expression") ;
            
            Item head = list.get(0) ;
            
            if ( head.isNode() )
            {
                if ( head.getNode().isVariable() && list.size() == 1 )
                {
                    // The case of (?z)
                    return new ExprVar(Var.alloc(head.getNode())) ;
                }
                return buildFunctionCall(list) ;
            }
            else if ( head.isList() )
                BuilderLib.broken(item, "Head is a list") ;
            else if ( head.isSymbol() )
            {
                if ( item.isTagged(Tags.tagExpr) )
                {
                    BuilderLib.checkLength(2, list, "Wrong length: "+item.shortString()) ;
                    item = list.get(1) ;
                    return buildItem(item) ;
                }
                
                return buildKnownFunction(list) ;                
            }
            throw new ARQInternalErrorException() ;
        }
    
        if ( item.isNode() )
        {
            if ( Var.isVar(item.getNode()) )
                return new ExprVar(Var.alloc(item.getNode())) ;
            return NodeValue.makeNode(item.getNode()) ;
        }
    
        if ( item.isSymbolIgnoreCase(Tags.tagTrue) )
            return NodeValue.TRUE ;
        if ( item.isSymbolIgnoreCase(Tags.tagFalse) )
            return NodeValue.FALSE ;
        
        BuilderLib.broken(item, "Not a list or a node or recognized symbol: "+item) ;
        return null ;
    }

    public BuilderExpr()
    {
        dispatch.put(Tags.tagRegex, buildRegex) ;
        dispatch.put(Tags.symEQ, buildEQ) ;
        dispatch.put(Tags.symNE, buildNE) ;
        dispatch.put(Tags.symGT, buildGT) ;
        dispatch.put(Tags.symLT, buildLT) ;
        dispatch.put(Tags.symLE, buildLE) ;
        dispatch.put(Tags.symGE, buildGE) ;
        dispatch.put(Tags.symOr, buildOr) ;     // Same builders for (or ..) and (|| ..)
        dispatch.put(Tags.tagOr, buildOr) ;
        dispatch.put(Tags.symAnd, buildAnd) ;   // Same builders for (and ..) and (&& ..)
        dispatch.put(Tags.tagAnd, buildAnd) ;
        dispatch.put(Tags.symPlus, buildPlus) ;
        dispatch.put(Tags.symMinus, buildMinus) ;
        dispatch.put(Tags.symMult, buildMult) ;
        dispatch.put(Tags.symDiv, buildDiv) ;
        dispatch.put(Tags.tagNot, buildNot) ;   // Same builders for (not ..) and (! ..)
        dispatch.put(Tags.symNot, buildNot) ;
        dispatch.put(Tags.tagStr, buildStr) ;
        dispatch.put(Tags.tagStrLang, buildStrLang) ;
        dispatch.put(Tags.tagStrDatatype, buildStrDatatype) ;
        dispatch.put(Tags.tagStr, buildStr) ;
        dispatch.put(Tags.tagRand, buildRand) ;
        
        dispatch.put(Tags.tagLang, buildLang) ;
        dispatch.put(Tags.tagLangMatches, buildLangMatches) ;
        dispatch.put(Tags.tagSameTerm, buildSameTerm) ;
        dispatch.put(Tags.tagDatatype, buildDatatype) ;
        dispatch.put(Tags.tagBound, buildBound) ;
        dispatch.put(Tags.tagCoalesce, buildCoalesce) ;
        dispatch.put(Tags.tagIf, buildConditional) ;
        dispatch.put(Tags.tagIRI, buildIsIRI) ;
        dispatch.put(Tags.tagURI, buildIsURI) ;
        dispatch.put(Tags.tagIsBlank, buildIsBlank) ;
        dispatch.put(Tags.tagIsLiteral, buildIsLiteral) ;
        dispatch.put(Tags.tagExists, buildExists) ;
        dispatch.put(Tags.tagNotExists, buildNotExists) ;
        
        dispatch.put(Tags.tagBNode, buildBNode) ;
        dispatch.put(Tags.tagIri, buildIri) ;
        dispatch.put(Tags.tagUri, buildUri) ;
        
        dispatch.put(Tags.tagIn, buildIn) ;
        dispatch.put(Tags.tagNotIn, buildNotIn) ;
        
        dispatch.put(Tags.tagCount, buildCount) ;
        dispatch.put(Tags.tagSum, buildSum) ;
        dispatch.put(Tags.tagMin, buildMin) ;
        dispatch.put(Tags.tagMax, buildMax) ;
        dispatch.put(Tags.tagAvg, buildAvg) ;
        dispatch.put(Tags.tagSample, buildSample) ;
        dispatch.put(Tags.tagGroupConcat, buildGroupConcat) ;
    }

    // See exprbuilder.rb

    static public interface Build { Expr make(ItemList list) ; }
    
    protected Build findBuild(String str)
    {
        for ( Iterator<String> iter = dispatch.keySet().iterator() ; iter.hasNext() ; )
        {
            String key = iter.next() ; 
            if ( str.equalsIgnoreCase(key) )    // ???
                return dispatch.get(key) ;
        }
        return null ;
    }
    
    protected Expr buildKnownFunction(ItemList list)
    {
        if ( list.size() == 0 )
            BuilderLib.broken(list, "Empty list for expression") ;
    
        Item item = list.get(0) ;
        String tag = item.getSymbol() ;
        if ( tag == null )
            BuilderLib.broken(item, "Null tag") ;
    
        Build b = findBuild(tag) ;
        if ( b == null )
            BuilderLib.broken(item, "No known symbol for "+tag) ;
        return b.make(list) ;
    }

    protected static Expr buildFunctionCall(ItemList list)
    {
        Item head = list.get(0) ;
        Node node = head.getNode() ;
        if ( node.isBlank() )
            BuilderLib.broken(head, "Blank node for function call!") ;
        if ( node.isLiteral() )
            BuilderLib.broken(head, "Literal node for function call!") ;
        ExprList args = buildExprListUntagged(list, 1) ;
        // Args
        return new E_Function(node.getURI(), args) ;
    }

 

    // ---- Dispatch objects
    // Can assume the tag is right (i.e. dispatched correctly) 
    // Specials
    
    final protected Build buildRegex = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(3, 4, list, "Regex: wanted 2 or 3 arguments") ;
            Expr expr = buildExpr(list.get(1)) ;
            Expr pattern = buildExpr(list.get(2)) ;
            Expr flags = null ;
            if ( list.size() != 3 )
                flags = buildExpr(list.get(3)) ;
            
            return new E_Regex(expr, pattern, flags) ;
        }
    };

    // Special
    final protected Build buildPlus = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(2, 3, list, "+: wanted 1 or 2 arguments") ;
            if ( list.size() == 2 )
            {
                Expr ex = buildExpr(list.get(1)) ;
                return new E_UnaryPlus(ex) ;
            }
            
            Expr left = buildExpr(list.get(1)) ;
            Expr right = buildExpr(list.get(2)) ;
            return new E_Add(left, right) ;
        }
    };

    final protected Build buildMinus = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(2, 3, list, "-: wanted 1 or 2 arguments") ;
            if ( list.size() == 2 )
            {
                Expr ex = buildExpr(list.get(1)) ;
                return new E_UnaryMinus(ex) ;
            }
            
            Expr left = buildExpr(list.get(1)) ;
            Expr right = buildExpr(list.get(2)) ;
            return new E_Subtract(left, right) ;
        }
    };
    
    final protected Build buildEQ = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(3, list, "=: wanted 2 arguments: got :"+list.size()) ;
            Expr left = buildExpr(list.get(1)) ;
            Expr right = buildExpr(list.get(2)) ;
            return new E_Equals(left, right) ;
        }
    };

    final protected Build buildNE = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(3, list, "!=: wanted 2 arguments: got :"+list.size()) ;
            Expr left = buildExpr(list.get(1)) ;
            Expr right = buildExpr(list.get(2)) ;
            return new E_NotEquals(left, right) ;
        }
    };

    final protected Build buildGT = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(3, list, ">: wanted 2 arguments: got :"+list.size()) ;
            Expr left = buildExpr(list.get(1)) ;
            Expr right = buildExpr(list.get(2)) ;
            return new E_GreaterThan(left, right) ;
        }
    };

    final protected Build buildLT = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(3, list, "<: wanted 2 arguments: got :"+list.size()) ;
            Expr left = buildExpr(list.get(1)) ;
            Expr right = buildExpr(list.get(2)) ;
            return new E_LessThan(left, right) ;
        }
    };

    final protected Build buildLE = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(3, list, "<=: wanted 2 arguments: got :"+list.size()) ;
            Expr left = buildExpr(list.get(1)) ;
            Expr right = buildExpr(list.get(2)) ;
            return new E_LessThanOrEqual(left, right) ;
        }
    };

    final protected Build buildGE = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(3, list, ">=: wanted 2 arguments: got :"+list.size()) ;
            Expr left = buildExpr(list.get(1)) ;
            Expr right = buildExpr(list.get(2)) ;
            return new E_GreaterThanOrEqual(left, right) ;
        }
    };

    final protected Build buildOr = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(3, list, "||: wanted 2 arguments: got :"+list.size()) ;
            Expr left = buildExpr(list.get(1)) ;
            Expr right = buildExpr(list.get(2)) ;
            return new E_LogicalOr(left, right) ;
        }
    };

    final protected Build buildAnd = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(3, list, "&&: wanted 2 arguments: got :"+list.size()) ;
            Expr left = buildExpr(list.get(1)) ;
            Expr right = buildExpr(list.get(2)) ;
            return new E_LogicalAnd(left, right) ;
        }
    };

    final protected Build buildMult = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(3, list, "*: wanted 2 arguments: got :"+list.size()) ;
            Expr left = buildExpr(list.get(1)) ;
            Expr right = buildExpr(list.get(2)) ;
            return new E_Multiply(left, right) ;
        }
    };

    final protected Build buildDiv = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(3, list, "/: wanted 2 arguments: got :"+list.size()) ;
            Expr left = buildExpr(list.get(1)) ;
            Expr right = buildExpr(list.get(2)) ;
            return new E_Divide(left, right) ;
        }
    };

    final protected Build buildNot = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(2, list, "!: wanted 1 arguments: got :"+list.size()) ;
            Expr ex = buildExpr(list.get(1)) ;
            return new E_LogicalNot(ex) ;
        }
    };

    final protected Build buildStr = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(2, list, "str: wanted 1 arguments: got :"+list.size()) ;
            Expr ex = buildExpr(list.get(1)) ;
            return new E_Str(ex) ;
        }
    };

    final protected Build buildStrLang = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(3, list, "strlang: wanted 2 arguments: got :"+list.size()) ;
            Expr ex1 = buildExpr(list.get(1)) ;
            Expr ex2 = buildExpr(list.get(2)) ;
            return new E_StrLang(ex1, ex2) ;
        }
    };

    final protected Build buildStrDatatype = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(3, list, "strlang: wanted 2 arguments: got :"+list.size()) ;
            Expr ex1 = buildExpr(list.get(1)) ;
            Expr ex2 = buildExpr(list.get(2)) ;
            return new E_StrDatatype(ex1, ex2) ;
        }
    };
    
    final protected Build buildRand = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(1, 2, list, "rand: wanted 0 or 1 arguments: got: "+list.size()) ;
            if ( list.size() == 1 )
                return new E_Random() ;
            
            Expr expr = buildExpr(list.get(1)) ;
            return new E_Random(expr) ;
        }
    };
    
    final protected Build buildLang = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(2, list, "lang: wanted 1 arguments: got :"+list.size()) ;
            Expr ex = buildExpr(list.get(1)) ;
            return new E_Lang(ex) ;
        }
    };

    final protected Build buildLangMatches = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(3, list, "langmatches: wanted 2 arguments: got :"+list.size()) ;
            Expr left = buildExpr(list.get(1)) ;
            Expr right = buildExpr(list.get(2)) ;
            return new E_LangMatches(left, right) ;
        }
    };

    final protected Build buildSameTerm = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(3, list, "sameterm: wanted 2 arguments: got :"+list.size()) ;
            Expr left = buildExpr(list.get(1)) ;
            Expr right = buildExpr(list.get(2)) ;
            return new E_SameTerm(left, right) ;
        }
    };

    final protected Build buildDatatype = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(2, list, "datatype: wanted 1 arguments: got :"+list.size()) ;
            Expr ex = buildExpr(list.get(1)) ;
            return new E_Datatype(ex) ;
        }
    };

    final protected Build buildBound = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(2, list, "bound: wanted 1 arguments: got :"+list.size()) ;
            Expr ex = buildExpr(list.get(1)) ;
            return new E_Bound(ex) ;
        }
    };

    final protected Build buildCoalesce = new Build()
    {
        public Expr make(ItemList list)
        {
            ExprList exprs = buildExprListUntagged(list, 1) ;
            return new E_Coalesce(exprs) ;
        }
    };

    final protected Build buildConditional = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(4, list, "IF: wanted 3 arguments: got :"+list.size()) ;
            Expr ex1 = buildExpr(list.get(1)) ;
            Expr ex2 = buildExpr(list.get(2)) ;
            Expr ex3 = buildExpr(list.get(3)) ;
            return new E_Conditional(ex1, ex2, ex3) ;
        }
    };

    final protected Build buildIsIRI = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(2, list, "isIRI: wanted 1 arguments: got :"+list.size()) ;
            Expr ex = buildExpr(list.get(1)) ;
            return new E_IsIRI(ex) ;
        }
    };

    final protected Build buildIsURI = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(2, list, "isURI: wanted 1 arguments: got :"+list.size()) ;
            Expr ex = buildExpr(list.get(1)) ;
            return new E_IsURI(ex) ;
        }
    };

    final protected Build buildIsBlank = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(2, list, "isBlank: wanted 1 arguments: got: "+list.size()) ;
            Expr ex = buildExpr(list.get(1)) ;
            return new E_IsBlank(ex) ;
        }
    };

    final protected Build buildIsLiteral = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(2, list, "isLiteral: wanted 1 arguments: got: "+list.size()) ;
            Expr ex = buildExpr(list.get(1)) ;
            return new E_IsLiteral(ex) ;
        }
    };
    
    final protected Build buildExists = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(2, list, "exists: wanted 1 arguments: got: "+list.size()) ;
            Op op = BuilderOp.build(list.get(1)) ;
            return new E_Exists(op) ;
        }
    };
    
    final protected Build buildNotExists = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(2, list, "notexists: wanted 1 arguments: got: "+list.size()) ;
            Op op = BuilderOp.build(list.get(1)) ;
            return new E_NotExists(op) ;
        }
    };
    
    final protected Build buildBNode = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(1, 2, list, "bnode: wanted 0 or 1 arguments: got: "+list.size()) ;
            if ( list.size() == 1 )
                return new E_BNode() ;
            
            Expr expr = buildExpr(list.get(1)) ;
            return new E_BNode(expr) ;
        }
    };
    
    final protected Build buildIri = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(2, list, "iri: wanted 1 argument: got: "+list.size()) ;
            Expr expr = buildExpr(list.get(1)) ;
            return new E_IRI(expr) ;
        }
    };
    
    final protected Build buildUri = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLength(2, list, "uri: wanted 1 argument: got: "+list.size()) ;
            Expr expr = buildExpr(list.get(1)) ;
            return new E_URI(expr) ;
        }
    };
    
    final protected Build buildIn = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLengthAtLeast(1, list, "in: wanted 1 or more arguments: got: "+list.size()) ;
            Item lhs = list.car() ;
            Expr expr = buildExpr(list.get(1)) ;
            ExprList eList = buildExprListUntagged(list, 2) ;
            return new E_OneOf(expr, eList) ;
        }
    };
    
    final protected Build buildNotIn = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderLib.checkLengthAtLeast(1, list, "notin: wanted 1 or more arguments: got: "+list.size()) ;
            Item lhs = list.car() ;
            Expr expr = buildExpr(list.get(1)) ;
            ExprList eList = buildExprListUntagged(list, 2) ;
            return new E_NotOneOf(expr, eList) ;
        }
    };
    
    // ---- Aggregate functions
    // (count)
    // (count distinct)
    // (count ?var)
    // (count distinct ?var)
    // Need a canonical name for the variable that will be set by the aggregation.
    // Aggregator.getVarName.
    
    static boolean startsWithDistinct(ItemList x) 
    {
        if ( x.size() > 0 && x.car().isSymbol(Tags.tagDistinct) )
            return true ;
        return false ;
    }
    
    // All the one expression cases
    static abstract class BuildAggCommon implements Build
    {
        public final Expr make(ItemList list)
        {
            ItemList x = list.cdr();    // drop "sum"
            boolean distinct = startsWithDistinct(x) ;
            if ( distinct )
                x = x.cdr();
            BuilderLib.checkLength(1, x, "Broken syntax: "+list.shortString()) ;
            // (sum ?var) 
            Expr expr = buildExpr(x.get(0)) ;
            return make(distinct, expr) ;
        }

        public abstract Expr make(boolean distinct, Expr expr) ;
    }
    
    final protected Build buildCount = new Build()
    {
        public Expr make(final ItemList list)
        {
            ItemList x = list.cdr();    // drop "count"
            boolean distinct = startsWithDistinct(x) ;
            if ( distinct )
                x = x.cdr();
            
            BuilderLib.checkLength(0, 1, x, "Broken syntax: "+list.shortString()) ;
            
            Aggregator agg = null ;
            if ( x.size() == 0 )
                agg = AggregatorFactory.createCount(distinct) ;
            else
            {
                Expr expr = BuilderExpr.buildExpr(x.get(0)) ;
                agg = AggregatorFactory.createCountExpr(distinct, expr) ;
            }
            return new ExprAggregator((Var)null, agg) ; 
        }
    };
    
    final protected Build buildSum = new BuildAggCommon()
    {
        @Override
        public Expr make(boolean distinct, Expr expr)
        {
            Aggregator agg = AggregatorFactory.createSum(distinct, expr) ;
            return new ExprAggregator((Var)null, agg) ; 
        }
    };
    
    final protected Build buildMin = new BuildAggCommon()
    {
        @Override
        public Expr make(boolean distinct, Expr expr)
        {
            Aggregator agg = AggregatorFactory.createMin(distinct, expr) ;
            return new ExprAggregator((Var)null, agg) ; 
        }
    };
    
    final protected Build buildMax = new BuildAggCommon()
    {
        @Override
        public Expr make(boolean distinct, Expr expr)
        {
            Aggregator agg = AggregatorFactory.createMax(distinct, expr) ;
            return new ExprAggregator((Var)null, agg) ; 
        }
    };

    final protected Build buildAvg = new BuildAggCommon()
    {
        @Override
        public Expr make(boolean distinct, Expr expr)
        {
            Aggregator agg = AggregatorFactory.createAvg(distinct, expr) ;
            return new ExprAggregator((Var)null, agg) ; 
        }
    };

    final protected Build buildSample = new BuildAggCommon()
    {
        @Override
        public Expr make(boolean distinct, Expr expr)
        {
            Aggregator agg = AggregatorFactory.createSample(distinct, expr) ;
            return new ExprAggregator((Var)null, agg) ; 
        }
    };
    
    final protected Build buildGroupConcat = new Build()
    {
        public Expr make(final ItemList list)
        {
            ItemList x = list.cdr();    // drop "group_concat"
            boolean distinct = startsWithDistinct(x) ;
            if ( distinct )
                x = x.cdr();
            
            // Complex syntax:
            // (groupConcat (separator "string) expr )
            if ( x.size() == 0 )
                BuilderLib.broken(list, "Broken syntax: "+list.shortString()) ;
            String separator = null ;
            if ( x.get(0).isTagged(Tags.tagSeparator))
            {
                ItemList y = x.get(0).getList() ;
                BuilderLib.checkLength(2, y, "Broken syntax: "+list) ;
                Node n = y.get(1).getNode() ;
                if ( ! n.isLiteral() || n.getLiteralDatatype() != null )
                    BuilderLib.broken(y, "Need string for separator: "+y) ;
                separator = n.getLiteralLexicalForm() ;
                x = x.cdr();
            }
            
            Expr expr = buildExpr(x.get(0)) ;
            Aggregator agg = AggregatorFactory.createGroupConcat(distinct, expr, separator) ;
            return new ExprAggregator((Var)null, agg) ; 
        }
    };

}


/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Information Ltd.
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
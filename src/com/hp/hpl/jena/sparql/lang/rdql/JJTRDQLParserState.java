/* Generated By:JJTree: Do not edit this line. ../src/com/hp/hpl/jena/query/parser/rdql\JJTRDQLParserState.java */

package com.hp.hpl.jena.sparql.lang.rdql;

class JJTRDQLParserState {
  private java.util.Stack nodes;
  private java.util.Stack marks;

  private int sp;		// number of nodes on stack
  private int mk;		// current mark
  private boolean node_created;

  JJTRDQLParserState() {
    nodes = new java.util.Stack();
    marks = new java.util.Stack();
    sp = 0;
    mk = 0;
  }

  /* Determines whether the current node was actually closed and
     pushed.  This should only be called in the final user action of a
     node scope.  */
  boolean nodeCreated() {
    return node_created;
  }

  /* Call this to reinitialize the node stack.  It is called
     automatically by the parser's ReInit() method. */
  void reset() {
    nodes.removeAllElements();
    marks.removeAllElements();
    sp = 0;
    mk = 0;
  }

  /* Returns the root node of the AST.  It only makes sense to call
     this after a successful parse. */
  RDQL_Node rootNode() {
    return (RDQL_Node)nodes.elementAt(0);
  }

  /* Pushes a node on to the stack. */
  void pushNode(RDQL_Node n) {
    nodes.push(n);
    ++sp;
  }

  /* Returns the node on the top of the stack, and remove it from the
     stack.  */
  RDQL_Node popNode() {
    if (--sp < mk) {
      mk = ((Integer)marks.pop()).intValue();
    }
    return (RDQL_Node)nodes.pop();
  }

  /* Returns the node currently on the top of the stack. */
  RDQL_Node peekNode() {
    return (RDQL_Node)nodes.peek();
  }

  /* Returns the number of children on the stack in the current node
     scope. */
  int nodeArity() {
    return sp - mk;
  }


  void clearNodeScope(RDQL_Node n) {
    while (sp > mk) {
      popNode();
    }
    mk = ((Integer)marks.pop()).intValue();
  }


  void openNodeScope(RDQL_Node n) {
    marks.push(new Integer(mk));
    mk = sp;
    n.jjtOpen();
  }


  /* A definite node is constructed from a specified number of
     children.  That number of nodes are popped from the stack and
     made the children of the definite node.  Then the definite node
     is pushed on to the stack. */
  void closeNodeScope(RDQL_Node n, int num) {
    mk = ((Integer)marks.pop()).intValue();
    while (num-- > 0) {
      RDQL_Node c = popNode();
      c.jjtSetParent(n);
      n.jjtAddChild(c, num);
    }
    n.jjtClose();
    pushNode(n);
    node_created = true;
  }


  /* A conditional node is constructed if its condition is true.  All
     the nodes that have been pushed since the node was opened are
     made children of the the conditional node, which is then pushed
     on to the stack.  If the condition is false the node is not
     constructed and they are left on the stack. */
  void closeNodeScope(RDQL_Node n, boolean condition) {
    if (condition) {
      int a = nodeArity();
      mk = ((Integer)marks.pop()).intValue();
      while (a-- > 0) {
	RDQL_Node c = popNode();
	c.jjtSetParent(n);
	n.jjtAddChild(c, a);
      }
      n.jjtClose();
      pushNode(n);
      node_created = true;
    } else {
      mk = ((Integer)marks.pop()).intValue();
      node_created = false;
    }
  }
}

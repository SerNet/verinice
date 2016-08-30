// $ANTLR 2.7.6 (2005-12-22): "vql.g" -> "VqlParser.java"$

package sernet.verinice.service.linktable.antlr;

import antlr.ASTFactory;
import antlr.ASTPair;
import antlr.NoViableAltException;
import antlr.ParserSharedInputState;
import antlr.RecognitionException;
import antlr.TokenBuffer;
import antlr.TokenStream;
import antlr.TokenStreamException;
import antlr.collections.AST;

public class VqlParser extends antlr.LLkParser       implements VqlParserTokenTypes
 {

protected VqlParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public VqlParser(TokenBuffer tokenBuf) {
  this(tokenBuf,1);
}

protected VqlParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public VqlParser(TokenStream lexer) {
  this(lexer,1);
}

public VqlParser(ParserSharedInputState state) {
  super(state,1);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

	public final void expr() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_AST = null;
		
		typeName();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop3:
		do {
			switch ( LA(1)) {
			case LINK:
			{
				linkedType();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case PARENT:
			{
				parentType();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case CHILD:
			{
				childType();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			default:
			{
				break _loop3;
			}
			}
		} while (true);
		}
		{
		switch ( LA(1)) {
		case LT:
		{
			linkType();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case PROP:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		property();
		astFactory.addASTChild(currentAST, returnAST);
		{
		switch ( LA(1)) {
		case LITERAL_AS:
		case LITERAL_as:
		{
			alias();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case EOF:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		expr_AST = (AST)currentAST.root;
		returnAST = expr_AST;
	}
	
	public final void typeName() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST typeName_AST = null;
		
		AST tmp1_AST = null;
		tmp1_AST = astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp1_AST);
		match(Alphanumeric);
		typeName_AST = (AST)currentAST.root;
		returnAST = typeName_AST;
	}
	
	public final void linkedType() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST linkedType_AST = null;
		
		AST tmp2_AST = null;
		tmp2_AST = astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp2_AST);
		match(LINK);
		typeName();
		astFactory.addASTChild(currentAST, returnAST);
		linkedType_AST = (AST)currentAST.root;
		returnAST = linkedType_AST;
	}
	
	public final void parentType() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST parentType_AST = null;
		
		AST tmp3_AST = null;
		tmp3_AST = astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp3_AST);
		match(PARENT);
		typeName();
		astFactory.addASTChild(currentAST, returnAST);
		parentType_AST = (AST)currentAST.root;
		returnAST = parentType_AST;
	}
	
	public final void childType() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST childType_AST = null;
		
		AST tmp4_AST = null;
		tmp4_AST = astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp4_AST);
		match(CHILD);
		typeName();
		astFactory.addASTChild(currentAST, returnAST);
		childType_AST = (AST)currentAST.root;
		returnAST = childType_AST;
	}
	
	public final void linkType() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST linkType_AST = null;
		
		AST tmp5_AST = null;
		tmp5_AST = astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp5_AST);
		match(LT);
		linkTypeName();
		astFactory.addASTChild(currentAST, returnAST);
		linkType_AST = (AST)currentAST.root;
		returnAST = linkType_AST;
	}
	
	public final void property() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST property_AST = null;
		
		AST tmp6_AST = null;
		tmp6_AST = astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp6_AST);
		match(PROP);
		propertyName();
		astFactory.addASTChild(currentAST, returnAST);
		property_AST = (AST)currentAST.root;
		returnAST = property_AST;
	}
	
	public final void alias() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST alias_AST = null;
		
		as();
		astFactory.addASTChild(currentAST, returnAST);
		aliasName();
		astFactory.addASTChild(currentAST, returnAST);
		alias_AST = (AST)currentAST.root;
		returnAST = alias_AST;
	}
	
	public final void as() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST as_AST = null;
		
		switch ( LA(1)) {
		case LITERAL_AS:
		{
			AST tmp7_AST = null;
			tmp7_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp7_AST);
			match(LITERAL_AS);
			as_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_as:
		{
			AST tmp8_AST = null;
			tmp8_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp8_AST);
			match(LITERAL_as);
			as_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = as_AST;
	}
	
	public final void linkTypeName() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST linkTypeName_AST = null;
		
		AST tmp9_AST = null;
		tmp9_AST = astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp9_AST);
		match(Alphanumeric);
		linkTypeName_AST = (AST)currentAST.root;
		returnAST = linkTypeName_AST;
	}
	
	public final void propertyName() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST propertyName_AST = null;
		
		AST tmp10_AST = null;
		tmp10_AST = astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp10_AST);
		match(Alphanumeric);
		propertyName_AST = (AST)currentAST.root;
		returnAST = propertyName_AST;
	}
	
	public final void aliasName() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST aliasName_AST = null;
		
		AST tmp11_AST = null;
		tmp11_AST = astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp11_AST);
        match(Alphanumeric);
		aliasName_AST = (AST)currentAST.root;
		returnAST = aliasName_AST;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"\"AS\"",
		"\"as\"",
		"LINK",
		"PARENT",
		"CHILD",
		"LT",
		"PROP",
		"Alphanumeric",
		"WS"
	};
	
	protected void buildTokenTypeASTClassMap() {
		tokenTypeToASTClassMap=null;
	};
	
	
	}

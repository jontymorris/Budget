package jontymorris.com.github;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jontymorris.com.github.TokenType.*;

public class Scanner {

	private final String source;
	private final List<Token> tokens;
	
	private static final Map<String, TokenType> keywords = new HashMap<>();
	static {
		keywords.put("and", AND);
		keywords.put("class", CLASS);
		keywords.put("else", ELSE);
		keywords.put("false", FALSE);
		keywords.put("fun", FUN);
		keywords.put("for", FOR);
		keywords.put("if", IF);
		keywords.put("nil", NIL);
		keywords.put("or", OR);
		keywords.put("print", PRINT);
		keywords.put("return", RETURN);
		keywords.put("super", SUPER);
		keywords.put("this", THIS);
		keywords.put("var", VAR);
		keywords.put("while", WHILE);
	}
	
	private int
		start = 0,
		current = 0,
		line = 1;
	
	public Scanner(String source) {
		this.source = source;
		tokens = new ArrayList<>();
	}
	
	public List<Token> scanTokens() {
		while (!isAtEnd()) {
			start = current;
			scanToken();
		}
		
		tokens.add(new Token(EOF, "", null, line));
		return tokens;
	}
	
	private void scanToken() {
		char c = advance();
		switch (c) {
		case '(':
			addToken(LEFT_PAREN);
			break;
		case ')':
			addToken(RIGHT_PAREN);
			break;
		case '{':
			addToken(LEFT_BRACE);
			break;
		case '}':
			addToken(RIGHT_BRACE);
			break;
		case ',':
			addToken(COMMA);
			break;
		case '.':
			addToken(DOT);
			break;
		case '-':
			addToken(MINUS);
			break;
		case '+':
			addToken(PLUS);
			break;
		case ';':
			addToken(SEMICOLON);
			break;
		case '*':
			addToken(STAR);
			break;
			
		case '!':
			addToken(match('=') ? BANG_EQUAL : BANG);
			break;
		case '=':
			addToken(match('=') ? EQUAL_EQUAL : EQUAL);
			break;
		case '<':
			addToken(match('=') ? LESS_EQUAL : LESS);
			break;
		case '>':
			addToken(match('=') ? GREATER_EQUAL : GREATER);
			break;
		
		case '/':
			if (match('/')) {
				// A comment goes to the end of the line
				while (peek() != '\n' && !isAtEnd()) advance();
			} else if (match('*')) {
				// Multiline comment
				multiComment();
			} else {
				addToken(SLASH);
			}
			break;
		
		case ' ':
		case '\r':
		case '\t':
			break;
		
		case '\n':
			line += 1;
			break;
		
		case '"':
			string();
			break;
			
		default:
			if (isDigit(c)) {
				number();
			} else if (isAlpha(c)) {
				identifier();
			} else {
				Budget.error(line, "Unexpected character.");
			}
			
			break;
		}
	}
	
	private void multiComment() {
		while (peek() != '*' && peekNext() != '/' && !isAtEnd()) {
			advance();
		}
		
		// Advance over final */
		advance();
		advance();
	}
	
	private void identifier() {
		while (isAlphaNumberic(peek())) {
			advance();
		}
		
		String text = source.substring(start, current);
		
		TokenType type = keywords.get(text);
		if (type == null) {
			type = IDENTIFIER;
		}
		
		addToken(type);
	}
	
	private boolean isAlphaNumberic(char c) {
		return isAlpha(c) || isDigit(c);
	}
	
	private boolean isAlpha(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c == '_');
	}
	
	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}
	
	private void number() {
		while (isDigit(peek())) {
			advance();
		}
		
		if (peek() == '.' && isDigit(peekNext())) {
			advance();
			
			while (isDigit(peek())) {
				advance();
			}
		}
		
		addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
	}
	
	private void string() {
		while (peek() != '"' && !isAtEnd()) {
			if (peek() == '\n') {
				line += 1;
			}
			advance();
		}
		
		// Unterminated string
		if (isAtEnd()) {
			Budget.error(line, "Unterminated string.");
			return;
		}
		
		// The closing "
		advance();
		
		// Trim the surrounding quotes
		String value = source.substring(start+1, current - 1);
		addToken(STRING, value);
	}
	
	private char peekNext() {
		if (current + 1 >= source.length()) {
			return '\0';
		}
		
		return source.charAt(current+1);
	}
	
	private char peek() {
		if (isAtEnd()) {
			return '\0';
		}
		
		return source.charAt(current);
	}
	
	private boolean match(char expected) {
		if (isAtEnd()) {
			return false;
		}
		
		if (source.charAt(current) != expected) {
			return false;
		}
		
		current ++;
		return true;
	}
	
	private void addToken(TokenType type) {
		addToken(type, null);
	}
	
	private void addToken(TokenType type, Object literal) {
		String text = source.substring(start, current);
		tokens.add(new Token(type, text, literal, line));
	}
	
	private char advance() {
		current ++;
		return source.charAt(current - 1);
	}
	
	private boolean isAtEnd() {
		return current >= source.length();
	}
	
}

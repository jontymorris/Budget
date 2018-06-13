package jontymorris.com.github;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Budget {

	static boolean hadError = false;
	
	public static void main(String[] args) {
		if (args.length > 1) {
			System.out.println("Usage: budget [script]");
		}
		
		else if (args.length == 1) {
			runFile(args[1]);
		}
		
		else {
			runPrompt();
		}
	}
	
	static void runFile(String file) {
		try {
			byte[] bytes = Files.readAllBytes(Paths.get(file));
			run(new String(bytes, Charset.defaultCharset()));
			
			if (hadError) {
				System.exit(0);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static void runPrompt() {
		InputStreamReader input = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(input);
		
		while (true) {
			try {
				System.out.print("> ");
				run(reader.readLine());
				
				hadError = false;
			} catch (IOException e) {
				break;
			}
		}
	}
	
	private static void run(String source) {
		Scanner scanner = new Scanner(source);
		List<Token> tokens = scanner.scanTokens();
		
		// TODO: For now we will just print out the tokens
		for (Token token: tokens) {
			System.out.println(token);
		}
	}
	
	static void error(int line, String message) {
		System.err.println("Error [Line " + line + "]: " + message);
		hadError = true;
	}
	
}

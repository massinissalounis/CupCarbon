package script;


public class Testtest {
	
	public static void main(String [] args) throws InterruptedException {
		Script s = new Script() ;

		s.add(CommandType.PSEND, 1000);
		s.add(CommandType.DELAY, 500);
		s.add(CommandType.PSEND, 2000);
		s.add(CommandType.DELAY, 800);
		//s.add(CommandType.BREAK, 0);
	
		System.out.println(s);
		
		System.out.println(s.next());
		System.out.println(s.next());
		System.out.println(s.next());
		System.out.println(s.next());
		System.out.println(s.next());
		System.out.println(s.next());
		System.out.println(s.next());
		System.out.println(s.next());
		System.out.println(s.next());
		System.out.println(s.next());
	}
}

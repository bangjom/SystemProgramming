import java.util.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


/**
 * 모든 instruction의 정보를 관리하는 클래스. instruction data들을 저장한다
 * 또한 instruction 관련 연산, 예를 들면 목록을 구축하는 함수, 관련 정보를 제공하는 함수 등을 제공 한다.
 */
public class InstTable {
	/** 
	 * inst.data 파일을 불러와 저장하는 공간.
	 *  명령어의 이름을 집어넣으면 해당하는 Instruction의 정보들을 리턴할 수 있다.
	 */
	HashMap<String, Instruction> instMap;
	
	/**
	 * 클래스 초기화. 파싱을 동시에 처리한다.
	 * @param instFile : instuction에 대한 명세가 저장된 파일 이름
	 */
	public InstTable(String instFile) {
		instMap = new HashMap<String, Instruction>();
		openFile(instFile);
	}
	
	/**
	 * 입력받은 이름의 파일을 열고 해당 내용을 파싱하여 instMap에 저장한다.
	 */
	public void openFile(String fileName) {
		try{
            //파일 객체 생성
			String dataFolder = System.getProperty("user.dir") + System.getProperty("file.separator") + "src\\";
			File file = new File(dataFolder+fileName);
            FileReader filereader = new FileReader(file);
            BufferedReader bufReader = new BufferedReader(filereader);
            String line = null;
            while((line = bufReader.readLine()) != null){
            	Instruction i = new Instruction(line);
            	instMap.put(i.instruction,i);
            }       
            bufReader.close();
        }catch (FileNotFoundException e) {
            // TODO: handle exception
        }catch(IOException e){
            System.out.println(e);
        }
	}
	
	//get, set, search 등의 함수는 자유 구현

}
/**
 * 명령어 하나하나의 구체적인 정보는 Instruction클래스에 담긴다.
 * instruction과 관련된 정보들을 저장하고 기초적인 연산을 수행한다.
 */
class Instruction {
	
	// int numberOfOperand;
	// String comment;
	/** instruction이 몇 바이트 명령어인지 저장. 이후 편의성을 위함 */
	
	String instruction;
	int opcode;
	int format;
	/**
	 * 클래스를 선언하면서 일반문자열을 즉시 구조에 맞게 파싱한다.
	 * @param line : instruction 명세파일로부터 한줄씩 가져온 문자열
	 */
	public Instruction(String line) {
		parsing(line);
	}
	
	/**
	 * 일반 문자열을 파싱하여 instruction 정보를 파악하고 저장한다.
	 * @param line : instruction 명세파일로부터 한줄씩 가져온 문자열
	 */
	public void parsing(String line)   {
		boolean output=true;
		char tmp;
		int temp=0;
		StringTokenizer st = new StringTokenizer(line," ");
		String token_k = st.nextToken();
		String token_v = st.nextToken();
		String token_f = st.nextToken();
		for(int i=0;i<token_v.length();i++) {
			tmp=token_v.charAt(i);
			if(Character.isDigit(tmp)==false) {
				output=false;
			}
		}
		if( output==false) {
			for(int i=0;i<2;i++) {
				tmp=token_v.charAt(i);
				if(token_v.charAt(i)=='A') temp+=10;
				else if(token_v.charAt(i)=='B') temp+=11;
				else if(token_v.charAt(i)=='C') temp+=12;
				else if(token_v.charAt(i)=='D') temp+=13;
				else if(token_v.charAt(i)=='E') temp+=14;
				else if(token_v.charAt(i)=='F') temp+=15;
				else temp+=(int)token_v.charAt(i)-48;
				if(i==0) temp=temp*16;
			}
		}else {
			temp = Integer.valueOf(token_v,16);
		}
		this.instruction = token_k.toString();
		this.opcode=temp;
		this.format=Integer.parseInt(token_f);
	}
	
		
	//그 외 함수 자유 구현
	
	
}

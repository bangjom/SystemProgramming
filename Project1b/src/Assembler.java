import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * Assembler : 
 * 이 프로그램은 SIC/XE 머신을 위한 Assembler 프로그램의 메인 루틴이다.
 * 프로그램의 수행 작업은 다음과 같다. 
 * 1) 처음 시작하면 Instruction 명세를 읽어들여서 assembler를 세팅한다. 
 * 2) 사용자가 작성한 input 파일을 읽어들인 후 저장한다. 
 * 3) input 파일의 문장들을 단어별로 분할하고 의미를 파악해서 정리한다. (pass1) 
 * 4) 분석된 내용을 바탕으로 컴퓨터가 사용할 수 있는 object code를 생성한다. (pass2) 
 * 
 * 
 * 작성중의 유의사항 : 
 *  1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은 안된다.
 *  2) 마찬가지로 작성된 코드를 삭제하지 않으면 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨.
 *  3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.
 *  4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)
 *  
 *     
 *  + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수 있습니다.
 */
public class Assembler {
	/** instruction 명세를 저장한 공간 */
	InstTable instTable;
	/** 읽어들인 input 파일의 내용을 한 줄 씩 저장하는 공간. */
	ArrayList<String> lineList;
	/** 프로그램의 section별로 symbol table을 저장하는 공간*/
	ArrayList<SymbolTable> symtabList;
	/** 프로그램의 section별로 literal table을 저장하는 공간*/
	ArrayList<LiteralTable> literaltabList;
	/** 프로그램의 section별로 프로그램을 저장하는 공간*/
	ArrayList<TokenTable> TokenList;
	/** 
	 * Token, 또는 지시어에 따라 만들어진 오브젝트 코드들을 출력 형태로 저장하는 공간.   
	 * 필요한 경우 String 대신 별도의 클래스를 선언하여 ArrayList를 교체해도 무방함.
	 */
	ArrayList<String> codeList;
	
	/**
	 * 클래스 초기화. instruction Table을 초기화와 동시에 세팅한다.
	 * 
	 * @param instFile : instruction 명세를 작성한 파일 이름. 
	 */
	public Assembler(String instFile) {
		instTable = new InstTable(instFile);
		lineList = new ArrayList<String>();
		symtabList = new ArrayList<SymbolTable>();
		literaltabList = new ArrayList<LiteralTable>();
		TokenList = new ArrayList<TokenTable>();
		codeList = new ArrayList<String>();
	}

	/** 
	 * 어셈블러의 메인 루틴
	 */
	public static void main(String[] args) {
		Assembler assembler = new Assembler("inst.txt");
		assembler.loadInputFile("input.txt");	
		assembler.pass1();
		assembler.printSymbolTable("symtab_20160270.txt");
		assembler.printLiteralTable("literaltab_20160270.txt");
		assembler.pass2();
		assembler.printObjectCode("output_20160270.txt");
		
	}

	/**
	 * inputFile을 읽어들여서 lineList에 저장한다.
	 * @param inputFile : input 파일 이름.
	 */
	private void loadInputFile(String inputFile) {
		try{
            //파일 객체 생성
			String dataFolder = System.getProperty("user.dir") + System.getProperty("file.separator") + "src\\";
			File file = new File(dataFolder+inputFile);
            FileReader filereader = new FileReader(file);
            BufferedReader bufReader = new BufferedReader(filereader);
            String line = null;
            while((line = bufReader.readLine()) != null){
            	lineList.add(line);
            }       
            bufReader.close();
        }catch (FileNotFoundException e) {
            // TODO: handle exception
        }catch(IOException e){
            System.out.println(e);
        }
	}

	/** 
	 * pass1 과정을 수행한다.
	 *   1) 프로그램 소스를 스캔하여 토큰단위로 분리한 뒤 토큰테이블 생성
	 *   2) label을 symbolTable에 정리
	 *   
	 *    주의사항 : SymbolTable과 TokenTable은 프로그램의 section별로 하나씩 선언되어야 한다.
	 */
	private void pass1() {
		int current=-1;  	//section 번호

		for(int i=0;i<lineList.size();i++) {
			if(lineList.get(i).contains("START") || lineList.get(i).contains("CSECT") ) { //section 만들기
				SymbolTable s = new SymbolTable();
				LiteralTable l = new LiteralTable();
				TokenTable t= new TokenTable(s,l,instTable);
				TokenList.add(t);
				literaltabList.add(l);
				symtabList.add(s);
				current++;
			}
			TokenList.get(current).putToken(lineList.get(i));			// section별로 token 집어넣기
		}
		
		for(int i=0;i<TokenList.size();i++) {
			for(int j=0;j<TokenList.get(i).tokenList.size();j++ ) {			// literaltable, symboltable만들기
				if(TokenList.get(i).tokenList.get(j).operand[0].contains("=") ) {
					String[] lit=TokenList.get(i).tokenList.get(j).operand[0].split("\'");
					if(TokenList.get(i).literalTab.search(lit[1])==-1) {
						TokenList.get(i).literalTab.putLiteral(lit[1], 0);
					}
				}
				if(TokenList.get(i).tokenList.get(j).label.matches("^[a-zA-Z]*$") && !TokenList.get(i).tokenList.get(j).label.matches("")){
					if(TokenList.get(i).literalTab.search(TokenList.get(i).tokenList.get(j).label)==-1) {
						TokenList.get(i).symTab.putSymbol(TokenList.get(i).tokenList.get(j).label, 0);
					}
				}
			}
		}
		
		for(int i=0;i<TokenList.size();i++) {
			for(int j=0;j<TokenList.get(i).tokenList.size();j++ ) {			// 리터럴 넣기
				if(TokenList.get(i).tokenList.get(j).operator.contains("LTORG")) {
					for(int z=0;z<TokenList.get(i).literalTab.literalList.size();z++) {
						String str="*\t"+TokenList.get(i).literalTab.literalList.get(z);
						TokenList.get(i).putToken(j+1,str);
					}
				}
				else if(TokenList.get(i).tokenList.get(j).operator.contains("END")){
					for(int z=0;z<TokenList.get(i).literalTab.literalList.size();z++) {
						String str="*\t"+TokenList.get(i).literalTab.literalList.get(z);
						TokenList.get(i).putToken(str);
					}
				}
			}
		}
		
		for(int i=0;i<TokenList.size();i++) {
			int currentLocation =0;
			ArrayList<Token> A=TokenList.get(i).tokenList;
			for(int j=0;j<TokenList.get(i).tokenList.size();j++ ) {
				A.get(j).location=currentLocation;
				String temp=A.get(j).operator.replace("+", "");
				if(A.get(j).label.contains(".")) continue;
				if(TokenList.get(i).getOpcode(temp)==-1) {	// 주석문이거나 명령어가 없는 라인의 주소처리
					if(A.get(j).operator.contains("RESW")) {
						currentLocation+=Integer.parseInt(A.get(j).operand[0])*3;
					}
					else if(A.get(j).operator.contains("RESB")) {
						currentLocation+=Integer.parseInt(A.get(j).operand[0]);
					}
					else if(A.get(j).operator.contains("EQU")) {
						if(A.get(j).operand[0].contains("*")) {
							continue;
						}
						else {
							String[] str=A.get(j).operand[0].split("-");
							int[] equTominus=new int[2];
							for(int k=0;k<j;k++) {
								if(A.get(k).label.contains(str[0])) equTominus[0]=A.get(k).location;
								else if(A.get(k).label.contains(str[1])) equTominus[1]=A.get(k).location;
							}
							A.get(j).location=equTominus[0]-equTominus[1];
						}
					}
					else if(A.get(j).operator.contains("BYTE") ) 
						currentLocation+=1;
					else if(A.get(j).operator.contains("WORD"))
						currentLocation+=3;
					else if(A.get(j).label.contains("*")) {
						if(Character.isDigit(A.get(j).operator.charAt(0))) currentLocation+=A.get(j).operator.length()/2;
						else currentLocation+=A.get(j).operator.length();
					}
				}
				else {	//instruction 라인 주소처리
					String str=A.get(j).operator.replace("+", "");
					currentLocation+=TokenList.get(i).getFormat(str);
					if(A.get(j).operator.contains("+"))		currentLocation+=1;
				}
			}
			for(int z=0;z<TokenList.get(i).tokenList.size();z++ ) {
				if( !A.get(z).label.matches("") && TokenList.get(i).symTab.search(A.get(z).label)==0) //symtable 주소처리
					TokenList.get(i).symTab.modifySymbol(A.get(z).label, A.get(z).location);
				if( !A.get(z).operator.matches("") && TokenList.get(i).literalTab.search(A.get(z).operator)==0) //literaltable 주소처리
					TokenList.get(i).literalTab.modifyLiteral(A.get(z).operator, A.get(z).location);
			}
			TokenList.get(i).size=currentLocation;	
		}
		
	}
	
	/**
	 * 작성된 SymbolTable들을 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printSymbolTable(String fileName) {
		try {
			FileWriter writer = new FileWriter(fileName);
			for(int i=0;i<TokenList.size();i++) {				
				for(int j=0;j<TokenList.get(i).symTab.symbolList.size();j++ ) {
					writer.write(TokenList.get(i).symTab.symbolList.get(j)+"\t\t"+Integer.toHexString(TokenList.get(i).symTab.locationList.get(j))+"\n");
				}
				writer.write("\n");
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * 작성된 LiteralTable들을 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printLiteralTable(String fileName) {
		
		 try {
			FileWriter writer = new FileWriter(fileName);
			for(int i=0;i<TokenList.size();i++) {				
				for(int j=0;j<TokenList.get(i).literalTab.literalList.size();j++ ) {
					writer.write(TokenList.get(i).literalTab.literalList.get(j)+" "+Integer.toHexString(TokenList.get(i).literalTab.locationList.get(j))+"\n");
				}
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * pass2 과정을 수행한다.
	 *   1) 분석된 내용을 바탕으로 object code를 생성하여 codeList에 저장.
	 */
	private void pass2() {
		String str="";
		//int[] count=new int[3];
		
		for(int i=0;i<TokenList.size();i++) {
			//count[i]=0;
			for(int j=0;j<TokenList.get(i).tokenList.size();j++) {
				TokenList.get(i).makeObjectCode(j);	
				//if(!TokenList.get(i).getObjectCode(j).matches("")) {
				//	count[i]++;
				//}	
			}
			
		}
		
		
		for(int i=0;i<TokenList.size();i++) {	//codeList에 저장
			ArrayList<Token> a=TokenList.get(i).tokenList;
			str="H"+TokenList.get(i).tokenList.get(0).label+"\t";	//H부분
			str=str.concat(String.format("%012X", TokenList.get(i).size));
			codeList.add(str);
			for(int j=0;j<TokenList.get(i).tokenList.size();j++) {
				if(a.get(0).operator.contains("EXTDEF")) {	//D부분
					str="D";
					for(int k=0;k<TokenList.get(i).tokenList.get(0).numberOfOperand;k++) 
						str=str.concat(a.get(0).operand[k]+TokenList.get(i).symTab.search(a.get(0).operand[k]));
					codeList.add(str);
				}
				else if(a.get(0).operator.contains("EXTREF")) {	//R부분
					str="R";
					for(int k=0;k<TokenList.get(i).tokenList.get(0).numberOfOperand;k++) {
						str=str.concat(a.get(0).operand[k]);
						if(k==0 && i==0) str=str.concat(" ");
					}
					codeList.add(str);
				}	
			}
			
			int count=0;
			String temp="";
			str=String.format("T%06X",0);
			for(int j=0;j<a.size();j++) {//T부분
				if(count+a.get(j).byteSize>30||a.get(j).operator.contains("LTORG")) {
					str=str.concat(String.format("%02X",count));
					str=str.concat(String.format("%s",temp));
					codeList.add(str);
					str=String.format("T%06X",a.get(j).location);
					temp="";
					count=0;
				}
				temp=temp.concat(a.get(j).objectCode);
				count+=a.get(j).byteSize;
			}
			str=str.concat(String.format("%02X",count));
			str=str.concat(String.format("%s",temp));
			codeList.add(str);
			
			String[] op=new String[0];
			for(int j=0;j<a.size();j++) {//m부분
				if(a.get(j).operator.contains("EXTREF")) {
					 op=new String[a.get(j).numberOfOperand];
					for(int k=0;k<op.length;k++)
						op[k]=a.get(j).operand[k];
				}
				if(op.length!=0) {
					for(int k=0;k<op.length;k++) {
						if(!a.get(j).operator.contains("EXTREF")&&a.get(j).operand[0].contains(op[k])) {
							if(!a.get(j).operand[0].contains("-")) {
								str=String.format("M%06X",a.get(j).location+1);
								str=str.concat(String.format("%02X",a.get(j+1).location-a.get(j).location+1));
								str=str.concat(String.format("+%s", a.get(j).operand[0]));
								codeList.add(str);
							}
							else {
								String[] tt=a.get(j).operand[0].split("-");
								str=String.format("M%06X",a.get(j).location);
								str=str.concat(String.format("%02X",2*(TokenList.get(i).size-a.get(j).location)));
								str=str.concat(String.format("+%s", tt[0]));
								codeList.add(str);
								str=String.format("M%06X",a.get(j).location);
								str=str.concat(String.format("%02X",2*(TokenList.get(i).size-a.get(j).location)));
								str=str.concat(String.format("-%s", tt[1]));
								codeList.add(str);
								break;
							}
							
						}
					}
				}
			}
			
			str="E";
			if(i==0)str=str.concat("000000");
			codeList.add(str);
			codeList.add("\n");
		}
	}
	
	/**
	 * 작성된 codeList를 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printObjectCode(String fileName) {
		try {
			FileWriter writer = new FileWriter(fileName);
			for(int i=0;i<codeList.size();i++) {				
					writer.write(codeList.get(i)+"\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}

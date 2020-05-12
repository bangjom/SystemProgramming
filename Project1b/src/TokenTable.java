import java.util.ArrayList;
import java.util.*;

/**
 * 사용자가 작성한 프로그램 코드를 단어별로 분할 한 후, 의미를 분석하고, 최종 코드로 변환하는 과정을 총괄하는 클래스이다. <br>
 * pass2에서 object code로 변환하는 과정은 혼자 해결할 수 없고 symbolTable과 instTable의 정보가 필요하므로 이를 링크시킨다.<br>
 * section 마다 인스턴스가 하나씩 할당된다.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND=3;
	
	/* bit 조작의 가독성을 위한 선언 */
	public static final int nFlag=32;
	public static final int iFlag=16;
	public static final int xFlag=8;
	public static final int bFlag=4;
	public static final int pFlag=2;
	public static final int eFlag=1;
	
	/* Token을 다룰 때 필요한 테이블들을 링크시킨다. */
	SymbolTable symTab;
	LiteralTable literalTab;
	InstTable instTab;
	int size;
	
	/** 각 line을 의미별로 분할하고 분석하는 공간. */
	ArrayList<Token> tokenList;
	/**
	 * 초기화하면서 symTable과 literalTable과 instTable을 링크시킨다.
	 * @param symTab : 해당 section과 연결되어있는 symbol table
	 * @param literalTab : 해당 section과 연결되어있는 literal table
	 * @param instTab : instruction 명세가 정의된 instTable
	 */
	public TokenTable(SymbolTable symTab,LiteralTable literalTab, InstTable instTab) {
		this.size=0;
		this.symTab=symTab;
		this.literalTab=literalTab;
		this.instTab=instTab;
		this.tokenList = new ArrayList<Token>();
	}
	/**
	 * 일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가한다.
	 * @param line : 분리되지 않은 일반 문자열
	 */
	public void putToken(String line) {
		tokenList.add(new Token(line));
	}
	public void putToken(int index,String line) {
		tokenList.add(index,new Token(line));
	}
	/**
	 * tokenList에서 index에 해당하는 Token을 리턴한다.
	 * @param index
	 * @return : index번호에 해당하는 코드를 분석한 Token 클래스
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}
	
	public int getOpcode(String operator) {
		int opcode=-1;
		String str=operator.replace("+", "");
		Set<String> keys=instTab.instMap.keySet();
		Iterator<String> it =keys.iterator();
		while(it.hasNext()) {
			String key = it.next();
			Instruction value = instTab.instMap.get(key);
			if(key.contains(str)) {
				return value.opcode;
			}
		}
		return opcode;
	}
	public int getFormat(String operator) {
		int format=0;
		Set<String> keys=instTab.instMap.keySet();
		Iterator<String> it =keys.iterator();
		while(it.hasNext()) {
			String key = it.next();
			Instruction value = instTab.instMap.get(key);
			if(key.contains(operator)) {
				return value.format;
			}
		}
		return format;
	}
	/**
	 * Pass2 과정에서 사용한다.
	 * instruction table, symbol table literal table 등을 참조하여 objectcode를 생성하고, 이를 저장한다.
	 * @param index
	 */
	public void makeObjectCode(int index){
		String result="";
		String str;
		//tokenList.get(index).byteSize=3;
		int temp=0;
		int opc=getOpcode(tokenList.get(index).operator);
		if(tokenList.get(index).label.contains(".")) return;
		if(opc!=-1) {// instruction 경우
			result=String.format("%02X", opc+2*tokenList.get(index).getFlag(nFlag)/32+tokenList.get(index).getFlag(iFlag)/16);
			result=result.concat(Integer.toHexString(tokenList.get(index).getFlag(xFlag)+tokenList.get(index).getFlag(pFlag)+tokenList.get(index).getFlag(eFlag)));
			if(tokenList.get(index).operand[0].contains("#")) { //다이렉트경우
				str=tokenList.get(index).operand[0].replace("#", "");
				temp=Integer.parseInt(str);
				result=result.concat(String.format("%03X", temp));
			}
			else if(tokenList.get(index).operand[0].contains("=")) {//리터럴경우
				String[] tok=tokenList.get(index).operand[0].split("'");
				temp=literalTab.search(tok[1])-tokenList.get(index+1).location;
				result=result.concat(String.format("%03X", temp));
			}
			else {
				if(tokenList.get(index).numberOfOperand==0) {	//오퍼랜드갯수가 0일떄
					result=result.concat("0000");
				}
				else if(tokenList.get(index).numberOfOperand==1) {	//오퍼랜드갯수가 1일떄
					if(getFormat(tokenList.get(index).operator)==2) {	//오퍼레이터가2형식일떄
						result=String.format("%02X", opc);
						if(tokenList.get(index).operand[0].matches("X")) result=result.concat("10");
						else if(tokenList.get(index).operand[0].matches("A")) result=result.concat("00");
						else if(tokenList.get(index).operand[0].matches("S")) result=result.concat("40");
						else if(tokenList.get(index).operand[0].matches("T")) result=result.concat("50");
						else if(tokenList.get(index).operand[0].matches("B")) result=result.concat("30");
						//tokenList.get(index).byteSize--;
					}
					else { 
						if(tokenList.get(index).operator.contains("+")) {//오퍼레이터가4형식일떄
							if(symTab.search(tokenList.get(index).operand[0])==-1) {
								result=result.concat("00000");
								//tokenList.get(index).byteSize=4;
							}
								
						}
						else {	//오퍼레이터가3형식일떄
							temp=symTab.search(tokenList.get(index).operand[0])-tokenList.get(index+1).location;
							if(temp<0) temp=4095+temp+1;
							result=result.concat(String.format("%03X", temp));
						}
					}
				}
				else if(tokenList.get(index).numberOfOperand==2) {
					if(getFormat(tokenList.get(index).operator)==2) {
						result=String.format("%02X", opc);
						for(int i=0;i<2;i++) {
							if(tokenList.get(index).operand[i].matches("X")) result=result.concat("1");
							else if(tokenList.get(index).operand[i].matches("A")) result=result.concat("0");
							else if(tokenList.get(index).operand[i].matches("S")) result=result.concat("4");
							else if(tokenList.get(index).operand[i].matches("T")) result=result.concat("5");
							else if(tokenList.get(index).operand[i].matches("B")) result=result.concat("3");
						}
						
						//tokenList.get(index).byteSize--;
					}
					else if(symTab.search(tokenList.get(index).operand[0])==-1) {
						result=result.concat("00000");
						//tokenList.get(index).byteSize=4;
					}
					
				}
			}	
		}
		else {////byte word =c
			if(tokenList.get(index).operator.contains("BYTE")) {
				String[] tok=tokenList.get(index).operand[0].split("'");
				result=tok[1];
				//tokenList.get(index).byteSize=tok[1].length()/2;
			}
			else if(tokenList.get(index).operator.contains("WORD")) {
				if(symTab.search(tokenList.get(index).operand[0])==-1)
					result=new String("000000");
				//tokenList.get(index).byteSize=3;
			}
			else if(tokenList.get(index).label.contains("*")) {
				if(Character.isDigit(tokenList.get(index).operator.charAt(0))) {
					result=tokenList.get(index).operator;
					//tokenList.get(index).byteSize=result.length()/2;
				}
					
				else
					for(int i=0;i<tokenList.get(index).operator.length();i++) {
						result=result.concat(String.format("%02X",(int)tokenList.get(index).operator.charAt(i)));
						//tokenList.get(index).byteSize=result.length()/2;
					}
						
			}
		}
		tokenList.get(index).objectCode=result;
		tokenList.get(index).byteSize=tokenList.get(index).objectCode.length()/2;
	}
	
	/** 
	 * index번호에 해당하는 object code를 리턴한다.
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}
	
}

/**
 * 각 라인별로 저장된 코드를 단어 단위로 분할한 후  의미를 해석하는 데에 사용되는 변수와 연산을 정의한다. 
 * 의미 해석이 끝나면 pass2에서 object code로 변형되었을 때의 바이트 코드 역시 저장한다.
 */
class Token{
	//의미 분석 단계에서 사용되는 변수들
	int location;
	int numberOfOperand;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;

	// object code 생성 단계에서 사용되는 변수들 
	String objectCode;
	int byteSize;
	
	/**
	 * 클래스를 초기화 하면서 바로 line의 의미 분석을 수행한다. 
	 * @param line 문장단위로 저장된 프로그램 코드
	 */
	public Token(String line) {
		location=0;
		nixbpe=0;
		byteSize=0;
		numberOfOperand=0;
		objectCode="";
		parsing(line);
	}
	
	/**
	 * line의 실질적인 분석을 수행하는 함수. Token의 각 변수에 분석한 결과를 저장한다.
	 * @param line 문장단위로 저장된 프로그램 코드.
	 */
	public void parsing(String line) {
		String[] tmp = new String[4];
		String[] tmp1 =line.split("\t");
		for(int i=0;i<4;i++)
			tmp[i]="";
		for(int i=0;i<tmp1.length;i++) 
			tmp[i]=tmp1[i];
		label=tmp[0];				//라벨 저장
		operator=tmp[1];			//operator 저장
		comment=tmp[3];				//comment 저장
		
		
		operand=new String[3];			//operand 저장
		String[] tmp2=tmp[2].split(",");
		String[] tmp3=new String[3];
		for(int i=0;i<3;i++)
			tmp3[i]="";
		for(int i=0;i<tmp2.length;i++)
			tmp3[i]=tmp2[i];
		operand[0]=tmp3[0];
		operand[1]=tmp3[1];
		operand[2]=tmp3[2];
		for(int i=0;i<3;i++) 
			if(!operand[i].matches("")) numberOfOperand++; 	//오퍼랜드 갯수 저장 
		if(tmp[2].contains("#")) {		//nixbpe 중 ni 결정
			setFlag(TokenTable.iFlag,1);
		}
		else if(tmp[2].contains("@")) {
			setFlag(TokenTable.nFlag,1);
		}
		else {
			setFlag(TokenTable.nFlag,1);
			setFlag(TokenTable.iFlag,1);
		}
		if(operand[1].contains("X")) setFlag(TokenTable.xFlag,1);	//nixbpe 중 x 결정
		if(operator.contains("+")) setFlag(TokenTable.eFlag,1); //nixbpe 중 e 결정
		if(!tmp[2].matches("") && getFlag(TokenTable.eFlag)==0 && !(getFlag(TokenTable.nFlag)==0 && getFlag(TokenTable.iFlag)==16)  ) //nixbpe 중 p결정
			setFlag(TokenTable.pFlag,1);
	}
	
	/** 
	 * n,i,x,b,p,e flag를 설정한다. 
	 * 
	 * 사용 예 : setFlag(nFlag, 1); 
	 *   또는     setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag : 원하는 비트 위치
	 * @param value : 집어넣고자 하는 값. 1또는 0으로 선언한다.
	 */
	public void setFlag(int flag, int value) {
		if(value==1) nixbpe |= flag;
		else  nixbpe &= flag;
		return;
	}
	
	/**
	 * 원하는 flag들의 값을 얻어올 수 있다. flag의 조합을 통해 동시에 여러개의 플래그를 얻는 것 역시 가능하다 
	 * 
	 * 사용 예 : getFlag(nFlag)
	 *   또는     getFlag(nFlag|iFlag)
	 * 
	 * @param flags : 값을 확인하고자 하는 비트 위치
	 * @return : 비트위치에 들어가 있는 값. 플래그별로 각각 32, 16, 8, 4, 2, 1의 값을 리턴할 것임.
	 */
	public int getFlag(int flags) {
		return nixbpe & flags;
	}
}

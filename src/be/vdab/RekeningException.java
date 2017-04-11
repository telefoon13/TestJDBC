// JDBC test By Mike D.
package be.vdab;

import java.io.Serializable;

public class RekeningException extends RuntimeException implements Serializable {

private static final long serialVersionUID = 1L;
	
	public RekeningException(){}
	
	public RekeningException(String fout){
		super(fout);
	}
	
	public RekeningException(String fout, Throwable cause){
		super(fout, cause);
	}
	
	public RekeningException(Throwable cause){
		super(cause);
	}
}

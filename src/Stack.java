
public class Stack {
	private int top;
	private Object[] stack;
	
	Stack(int capacity){
		stack = new Object[capacity];
		top=-1;
	}
	
	public void push(Object Data) {
		if (!isFull()){
			top++;
			stack[top]=Data;
		}
	}
	
	
	public Object pop() {
		if(!isEmpty()) {
			Object Data = stack[top];
			stack[top]=null;
			top--;
			return Data;
		}
		else {
			return null;
		}
	}
	 public Object peek() {
		 if(!isEmpty()) {
				Object Data = stack[top];
				return Data;
			}
			else {
				return null;
			}
	 }
	 public boolean isEmpty() {
		 if(top==-1) {
			 return true;
		 }else {
			 return false;
		 }
			 
	 }
	 
	 public boolean isFull() {
		 if(stack.length==top+1) {
			 return true;
		 }else {
			 return false;
		 }
	 }
	 public int size() {
		 return top+1;
	 }
	
}

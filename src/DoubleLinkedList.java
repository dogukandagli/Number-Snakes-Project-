
public class DoubleLinkedList {
	Node2 head = null;
	
	
	public void add(Object newData) {
		Node2 newNode = new Node2(newData);
		if (head == null) {
			this.head = newNode;
		}else {
			Node2 temp = this.head;
			while(temp.next != null) {
				temp = temp.next;
			}
			temp.next = newNode;
			newNode.prev = temp;
		}
	}
}

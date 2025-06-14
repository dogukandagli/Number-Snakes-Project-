
public class SingleLinkedList {
	private Node head;

	public void setHead(Node head) {
		this.head = head;
	}

	public void add(Object dataToAdd) {

		Node newnode = new Node(dataToAdd);
		newnode.setLink(head); // yeni elemanın bağlantısı eski head'e olur
		head = newnode; // head artık yeni eleman olur

	}

	public void delete(Object dataToDelete) {
	    if (head == null) return;

	    // Baştaki eşleşen düğümleri sil
	    while (head != null && head.getData().equals(dataToDelete)) {
	        head = head.getLink();
	    }

	    Node current = head;
	    Node previous = null;

	    while (current != null) {
	        if (current.getData().equals(dataToDelete)) {
	            previous.setLink(current.getLink());
	            current = previous.getLink();
	        } else {
	            previous = current;
	            current = current.getLink();
	        }
	    }
	}


	public boolean search(Object item) {
		boolean flag = false;

		if (head == null) {
			
		} else {
			Node temp = head;
			while (temp != null) {
				if ((char) item == (char) temp.getData()) {
					flag = true;
					break;
				}
				temp = temp.getLink();
			}
		}

		return flag;
	}

	public void print() {
		if (head == null) {
			
		} else {
			Node temp = head;
			while (temp != null) {
				System.out.print(temp.getData() + " -> ");
				temp = temp.getLink();
			}
			System.out.println("null");
		}
	}

	public int size() {
		int count = 0;

		if (head == null) {
			
		} else {
			Node temp = head;
			while (temp != null) {
				count++;
				temp = temp.getLink();
			}
		}

		return count;
	}

	public char[] toArray() {
		char[] arr = new char[size()];
		Node curr = head;
		int i = 0;
		while (curr != null) {
			arr[i++] = (char) curr.getData();
			curr = curr.getLink();
		}
		return arr;
	}

	public Node getHead() {
		return head;
	}
}
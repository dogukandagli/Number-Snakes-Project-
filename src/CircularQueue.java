
public class CircularQueue {
	int rear = -1;
	int front = 0;
	Object[] queue;

	public CircularQueue(int size) {
		queue = new Object[size];
	}

	public void enqueue(Object item) {
		rear = (rear + 1) % queue.length;
		queue[rear] = item;
	}

	public Object dequeue() {
		Object item = queue[front];
		queue[front] = null;
		front = (front + 1) % queue.length;
		return item;
	}

	public Object peek() {
		return queue[front];
	}

	public boolean isEmpty() {
		return (queue[front] == null);
	}

	public boolean isFull() {
		return (rear + 1) % queue.length == front && queue[front] != null && queue[rear] != null;
	}

	public int size() {
		if (queue[front] == null) {
			return 0;
		} else {
			if (rear >= front)
				return rear - front + 1;
			else
				return queue.length - (front - rear) + 1;
		}
	}
}

public class GenericLinkedStack<E> implements Stack<E> {

	private static class Elem<T> {
		private T value;
		private Elem<T> next;

		private Elem(T value, Elem<T> next) {
			this.value = value;
			this.next = next;
		}
	}

	private Elem<E> top;

	public GenericLinkedStack() {
		top = new Elem<E>(null,null);
	}

	public boolean isEmpty() {
		return top.value == null;
	}

	public E peek() throws EmptyStackException {
		if(top.value == null) {
			throw new EmptyStackException("Stack is empty");
		}
		return top.value;
	}

	public E pop() throws EmptyStackException {
		E info = top.value;
		Elem<E> oldTop = top;
		Elem<E> newTop = top.next;

		top = newTop;
		oldTop.value = null;
		oldTop.next = null;

		if(info == null) {
			throw new EmptyStackException("Stack is empty");
		}
		return info;
	}

	public void push(E value) throws EmptyStackException {
		top = new Elem<E>(value,top);
		if(value == null) {
			throw new EmptyStackException("Stack is empty");
		}
	}
}
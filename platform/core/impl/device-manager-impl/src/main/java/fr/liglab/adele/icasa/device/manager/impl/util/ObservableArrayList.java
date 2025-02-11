/**
 *
 *   Copyright 2011-2013 Universite Joseph Fourier, LIG, ADELE Research
 *   Group Licensed under a specific end user license agreement;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://adeleresearchgroup.github.com/iCasa/snapshot/license.html
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.icasa.device.manager.impl.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ObservableArrayList<E> extends ArrayList<E> {

	private List<ArrayListObserver<E>> observers = null;
	private static final long serialVersionUID = 1L;

	public ObservableArrayList(ArrayListObserver<E> observer) {

		registerObserver(observer);

	}

	@Override
	public boolean add(E e) {

		boolean result = super.add(e);

		if (result) {

			for (ArrayListObserver<E> o : getObservers()) {

				o.onAdd(e);

			}

		}

		return result;

	}

	@Override
	public void add(int index, E element) {

		super.add(index, element);

		for (ArrayListObserver<E> o : getObservers()) {

			o.onAdd(index, element);

		}

	}

	@Override
	public boolean addAll(Collection<? extends E> c) {

		boolean result = super.addAll(c);

		if (result) {

			for (ArrayListObserver<E> o : getObservers()) {

				o.onAddAll(c);

			}

		}

		return result;

	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {

		boolean result = super.addAll(index, c);

		for (ArrayListObserver<E> o : getObservers()) {

			o.onAddAll(index, c);

		}

		return result;

	}

	@Override
	public void clear() {

		super.clear();

		for (ArrayListObserver<E> o : getObservers()) {

			o.onClear();

		}

	}

	public List<ArrayListObserver<E>> getObservers() {

		if (observers == null) {

			observers = new ArrayList<ArrayListObserver<E>>();

		}

		return observers;

	}

	public void registerObserver(ArrayListObserver<E> observer) {

		getObservers().add(observer);

	}

	@Override
	public E remove(int index) {

		E toRet = super.remove(index);

		for (ArrayListObserver<E> o : getObservers()) {

			o.onRemove(index);

		}

		return toRet;

	}

	@Override
	public boolean remove(Object obj) {

		boolean result = super.remove(obj);

		if (result) {

			for (ArrayListObserver<E> o : getObservers()) {

				o.onRemove(obj);

			}

		}

		return result;

	}

	@Override
	public boolean removeAll(Collection<?> c) {

		boolean result = super.removeAll(c);

		for (ArrayListObserver<E> o : getObservers()) {

			o.onRemoveAll(c);

		}

		return result;

	}

	@Override
	public boolean retainAll(Collection<?> c) {

		boolean result = super.retainAll(c);

		for (ArrayListObserver<E> o : getObservers()) {

			o.onRetainAll(c);

		}

		return result;

	}

	@Override
	public E set(int index, E element) {

		E toRet = super.set(index, element);

		for (ArrayListObserver<E> o : getObservers()) {

			o.onSet(index, element);

		}

		return toRet;

	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {

		List<E> toRet = super.subList(fromIndex, toIndex);

		for (ArrayListObserver<E> o : getObservers()) {

			o.onSubList(fromIndex, toIndex);

		}

		return toRet;

	}

	public void unregisterObserver(ArrayListObserver<E> observer) {

		getObservers().remove(observer);

	}

}
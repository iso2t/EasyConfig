package com.iso2t.easyconfig.api.value.wrappers;

import com.iso2t.easyconfig.api.value.AbstractValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a configuration value that wraps a list of elements of type T.
 * This class extends the AbstractValue class to manage list-based configurations,
 * supporting operations for retrieving, modifying, and managing default list values.
 *
 * @param <T> the type of elements in the list
 */
public class ListValue<T> extends AbstractValue<List<T>> {

	private ListValue (List<T> defaultList) {
		super(new ArrayList<>(defaultList));
	}

	@Override
	public List<T> get () {
		return super.get();
	}

	@Override
	public void set (List<T> newValue) {
		super.set(new ArrayList<>(newValue));
	}

	public void add (T element) {
		get().add(element);
	}

	public void remove (T element) {
		get().remove(element);
	}

	public void clear () {
		get().clear();
	}

	public void addAll (Collection<? extends T> c) {
		get().addAll(c);
	}
	
	@SafeVarargs
	public static <T> ListValue<T> of (T... values) {
		return new ListValue<>(List.of(values));
	}
	
	public static <T> ListValue<T> of (List<T> list) {
		return new ListValue<>(list);
	}
	
}

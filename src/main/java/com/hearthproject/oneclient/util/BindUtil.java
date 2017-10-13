package com.hearthproject.oneclient.util;

import com.google.common.collect.Lists;
import javafx.beans.WeakListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;

public class BindUtil {

    public static <E, R> void bindMapping(ObservableList<R> in, ObservableList<E> out, Function<R, E> function) {
        final MappedBinding<E, R> contentBinding = new MappedBinding<>(out, function);
        if (out != null) {
            out.setAll(map(Lists.newArrayList(in), function));
        }
        in.removeListener(contentBinding);
        in.addListener(contentBinding);
    }

    private static <E, R> ObservableList<E> map(List<? extends R> in, Function<R, E> map) {
        return in.stream().map(map).collect(toObservableList());
    }

    private static class MappedBinding<E, R> implements ListChangeListener<R>, WeakListener {

        private final WeakReference<List<E>> listRef;
        private final Function<R, E> function;

        public MappedBinding(List<E> list, Function<R, E> function) {
            this.listRef = new WeakReference<>(list);
            this.function = function;
        }

        @Override
        public void onChanged(Change<? extends R> change) {
            final List<E> list = listRef.get();
            if (list == null) {
                change.getList().removeListener(this);
            } else {
                while (change.next()) {
                    if (change.wasPermutated()) {
                        list.subList(change.getFrom(), change.getTo()).clear();
                        List<? extends R> changes = change.getList().subList(change.getFrom(), change.getTo());
                        MiscUtil.runLaterIfNeeded(() -> list.addAll(change.getFrom(), map(changes, function)));
                    } else {
                        if (change.wasRemoved()) {
                            MiscUtil.runLaterIfNeeded(() -> list.subList(change.getFrom(), change.getFrom() + change.getRemovedSize()).clear());
                        }
                        if (change.wasAdded()) {
                            List<? extends R> changes = change.getAddedSubList();
                            MiscUtil.runLaterIfNeeded(() -> list.addAll(change.getFrom(), map(changes, function)));
                        }
                    }
                }
            }
        }

        @Override
        public boolean wasGarbageCollected() {
            return listRef.get() == null;
        }

        @Override
        public int hashCode() {
            final List<E> list = listRef.get();
            return (list == null) ? 0 : list.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            final List<E> list1 = listRef.get();
            if (list1 == null) {
                return false;
            }

            if (obj instanceof MappedBinding) {
                final MappedBinding<?, ?> other = (MappedBinding<?, ?>) obj;
                final List<?> list2 = other.listRef.get();
                return list1 == list2;
            }
            return false;
        }
    }


    public static <T> Collector<T, ?, ObservableList<T>> toObservableList() {
        return Collector.of(
                FXCollections::observableArrayList,
                ObservableList::add,
                (l1, l2) -> {
                    l1.addAll(l2);
                    return l1;
                });
    }


}
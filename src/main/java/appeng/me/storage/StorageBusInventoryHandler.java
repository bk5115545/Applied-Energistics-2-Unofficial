package appeng.me.storage;

import java.util.function.Predicate;

import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.ItemFilterList;
import appeng.util.item.NetworkItemList;

public class StorageBusInventoryHandler<T extends IAEStack<T>> extends MEInventoryHandler<T> {

    public StorageBusInventoryHandler(IMEInventory<T> i, StorageChannel channel) {
        super(i, channel);
    }

    @Override
    public IItemList<T> getAvailableItems(final IItemList<T> out, int iteration) {
        return this.getAvailableItems(out, iteration, null);
    }

    @Override
    public IItemList<T> getAvailableItems(final IItemList<T> out, int iteration, Predicate<T> preFilter) {
        if (!this.hasReadAccess && !isVisible()) {
            return out;
        }

        if (out instanceof ItemFilterList) return this.getAvailableItemsFilter(out, iteration, preFilter);

        Predicate<T> filterCondition = preFilter;

        if (this.isExtractFilterActive() && !this.getExtractPartitionList().isEmpty()) {
            Predicate<T> extractFilter = this.getExtractFilterCondition();
            filterCondition = filterCondition == null ? extractFilter : extractFilter.and(filterCondition);
        }

        return this.getAvailableItemsInternal(out, iteration, filterCondition == null ? e -> true : filterCondition);
    }

    @Override
    protected IItemList<T> filterAvailableItems(IItemList<T> out, int iteration) {
        Predicate<T> filterCondition = this.getExtractFilterCondition();
        return getAvailableItemsInternal(out, iteration, filterCondition);
    }

    @SuppressWarnings("unchecked")
    private IItemList<T> getAvailableItemsInternal(IItemList<T> out, int iteration, Predicate<T> filterCondition) {
        final IItemList<T> availableItems = this.getInternal()
                .getAvailableItems((IItemList<T>) this.getChannel().createList(), iteration, filterCondition);
        if (availableItems instanceof NetworkItemList) {
            NetworkItemList<T> networkItemList = new NetworkItemList<>((NetworkItemList<T>) availableItems);
            networkItemList.addFilter(filterCondition);
            return networkItemList;
        } else {
            for (T items : availableItems) {
                if (filterCondition.test(items)) {
                    out.add(items);
                }
            }
            return out;
        }
    }
}

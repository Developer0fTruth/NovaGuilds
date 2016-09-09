package co.marcin.novaguilds.impl.storage.managers;

import co.marcin.novaguilds.NovaGuilds;
import co.marcin.novaguilds.api.storage.Resource;
import co.marcin.novaguilds.api.storage.ResourceManager;
import co.marcin.novaguilds.api.storage.Storage;

import java.util.Collection;
import java.util.HashSet;

public abstract class AbstractResourceManager<T extends Resource> implements ResourceManager<T> {
	protected final NovaGuilds plugin = NovaGuilds.getInstance();
	private final Storage storage;
	private final Collection<T> removalQueue = new HashSet<>();
	private final Collection<T> saveQueue = new HashSet<>();

	/**
	 * The constructor
	 *
	 * @param storage the storage
	 * @param clazz   type class
	 */
	protected AbstractResourceManager(Storage storage, Class clazz) {
		this.storage = storage;
		register(clazz);
	}

	@Override
	public Integer save(Collection<T> list) {
		int count = 0;

		for(T t : list) {
			if(save(t)) {
				count++;
			}
		}

		return count;
	}

	@Override
	public int remove(Collection<T> list) {
		int count = 0;

		for(T t : list) {
			if(remove(t)) {
				count++;
			}
		}

		return count;
	}

	/**
	 * Gets the storage
	 *
	 * @return the storage
	 */
	protected Storage getStorage() {
		return storage;
	}

	/**
	 * Registers the manager
	 *
	 * @param clazz type class
	 */
	private void register(Class clazz) {
		getStorage().registerResourceManager(clazz, this);
	}

	@Override
	public int executeRemoval() {
		int count = remove(removalQueue);
		removalQueue.clear();
		return count;
	}

	@Override
	public void addToSaveQueue(T t) {
		saveQueue.add(t);
	}

	@Override
	public void removeFromSaveQueue(T t) {
		if(isInSaveQueue(t)) {
			saveQueue.remove(t);
		}
	}

	@Override
	public boolean isInSaveQueue(T t) {
		return saveQueue.contains(t);
	}

	@Override
	public void addToRemovalQueue(T t) {
		removalQueue.add(t);
	}

	@Override
	public boolean isInRemovalQueue(T t) {
		return removalQueue.contains(t);
	}

	@Override
	public int executeSave() {
		int count = save(saveQueue);
		saveQueue.clear();
		return count;
	}
}

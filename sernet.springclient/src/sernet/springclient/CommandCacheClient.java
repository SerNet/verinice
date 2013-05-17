/*******************************************************************************
 * Copyright (c) 2012 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.springclient;

import java.util.ArrayList;
import java.util.Properties;
import java.util.UUID;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;
import net.sf.ehcache.Status;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.interfaces.ICommand;
import sernet.verinice.interfaces.ICommandCacheClient;
import sernet.verinice.interfaces.ICommandService;

/**
 *
 */
public class CommandCacheClient implements ICommandCacheClient {
    
    private ICommandService commandService;
    
    
    private transient CacheManager manager = null;
    private String globalCacheId = null;
    private transient Cache globalCache = null;
    private transient Logger log;
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommandCacheClient#executeCachableCommand(sernet.verinice.interfaces.GenericCommand)
     */
    @Override
    public ICommand executeCachableCommand(ICommand command) throws CommandException {
        if(command instanceof ICachedCommand){
            ICachedCommand cacheCommand = (ICachedCommand)command;
            if(getLog().isDebugEnabled()){
                logCacheStatistics();
            }
            
            if(getGlobalCache().isKeyInCache(cacheCommand.getCacheID()) 
                    && getGlobalCache().get(cacheCommand.getCacheID()) != null 
                    && getGlobalCache().get(cacheCommand.getCacheID()).getValue() != null){
                Object cachedValue = getGlobalCache().get(cacheCommand.getCacheID()).getValue();
                if(getLog().isDebugEnabled() && cachedValue instanceof ArrayList<?>){
                    ArrayList<?> list = (ArrayList<?>)cachedValue;
                    getLog().debug("injecting List with " + list.size() + " elements into:\t" + cacheCommand.getCacheID());
                }
                cacheCommand.injectCacheResult(cachedValue);
                return cacheCommand;
            } else {
                command = getCommandService().executeCommand((GenericCommand)command);
                cacheCommand = (ICachedCommand)command;

                Object cachedValue = cacheCommand.getCacheableResult();
                if(getLog().isDebugEnabled() && cachedValue instanceof ArrayList<?>){
                    ArrayList<?> list = (ArrayList<?>)cachedValue;
                    getLog().debug("putting List with " + list.size() + " elements from "  + cacheCommand.getCacheID() + " to Cache");
                }
                getGlobalCache().put(new Element(cacheCommand.getCacheID(), cacheCommand.getCacheableResult()));
                return command;
            }

        } else {
            return getCommandService().executeCommand((GenericCommand)command);
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommandCacheClient#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return false;
    }

    public ICommandService getCommandService() {
        return commandService;
    }
    
    public void setCommandService(ICommandService commandService) {
        this.commandService = commandService;
    }
    
    private Cache getGlobalCache() {
        if (manager == null || Status.STATUS_SHUTDOWN.equals(manager.getStatus()) || globalCache == null || !Status.STATUS_ALIVE.equals(globalCache.getStatus())) {
            globalCache = createCache();
        } else {
            globalCache = manager.getCache(globalCacheId);
        }
        return globalCache;
    }
    
    private Cache createCache() {
        final int maxElementsInMemory = 20000;
        final int timeToLiveSeconds = 600;
        final int timeToIdleSeconds = 500;
        globalCacheId = UUID.randomUUID().toString();
        manager = CacheManager.create();
        globalCache = new Cache(globalCacheId, maxElementsInMemory, false, false, timeToLiveSeconds, timeToIdleSeconds);
        manager.addCache(globalCache);
        if(getLog().isDebugEnabled()){
            getLog().debug("Global Report Cache created! Storing cache in:\t" + manager.getDiskStorePath());
        }
        
        return globalCache;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommandService#executeCommand(sernet.verinice.interfaces.ICommand)
     */
    @Override
    public <T extends ICommand> T executeCommand(T command) throws CommandException {
        return (T) executeCachableCommand(command);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommandService#discardUserData()
     */
    @Override
    public void discardUserData() {
        getCommandService().discardUserData();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommandService#getProperties()
     */
    @Override
    public Properties getProperties() {
        return getCommandService().getProperties();
    }
    
    @Override
    public void resetCache(){
        getGlobalCache().removeAll();
    }
    
    private Logger getLog(){
        if(log == null){
            log = Logger.getLogger(CommandCacheClient.class);
        }
        return log;
    }
    
    private void logCacheStatistics(){
        Statistics stats = getGlobalCache().getStatistics();
        getLog().debug("################start Cache Stats###################");
        getLog().debug("AvgGetTime:\t" + stats.getAverageGetTime());
        getLog().debug("CacheHits:\t" + stats.getCacheHits());
        getLog().debug("CacheMisses:\t" + stats.getCacheMisses());
        getLog().debug("DiskStoreObjectCount:\t" + stats.getDiskStoreObjectCount());
        getLog().debug("EvictionCount:\t" + stats.getEvictionCount());
        getLog().debug("InMemoryHits:\t" + stats.getInMemoryHits());
        getLog().debug("MemoryStoreObjectCount:\t" + stats.getMemoryStoreObjectCount());
        getLog().debug("ObjectCount:\t" + stats.getObjectCount());
        getLog().debug("OnDiskHits:\t" + stats.getOnDiskHits());
        getLog().debug("StatisticAccuracy:\t" + stats.getStatisticsAccuracy());
        getLog().debug("StatisticAccuracyDescription:\t" + stats.getStatisticsAccuracyDescription());
        getLog().debug("################end Cache Stats###################");
    }

}

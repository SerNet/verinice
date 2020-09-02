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

import org.apache.log4j.Logger;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;
import net.sf.ehcache.Status;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.interfaces.ICommand;
import sernet.verinice.interfaces.ICommandCacheClient;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.service.sync.VnaSchemaVersion;

/**
 *
 */
public class CommandCacheClient implements ICommandCacheClient {
    
    private ICommandService commandService;
    
    
    private transient CacheManager manager = null;
    private String globalCacheId = null;
    private transient Cache globalCache = null;
    private static final Logger log = Logger.getLogger(CommandCacheClient.class);
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommandCacheClient#executeCachableCommand(sernet.verinice.interfaces.GenericCommand)
     */
    @Override
    public ICommand executeCachableCommand(ICommand command) throws CommandException {
        if(command instanceof ICachedCommand){
            ICachedCommand cacheCommand = (ICachedCommand)command;
            if(log.isDebugEnabled()){
                logCacheStatistics();
            }
            
            if(getGlobalCache().isKeyInCache(cacheCommand.getCacheID()) 
                    && getGlobalCache().get(cacheCommand.getCacheID()) != null 
                    && getGlobalCache().get(cacheCommand.getCacheID()).getValue() != null){
                Object cachedValue = getGlobalCache().get(cacheCommand.getCacheID()).getValue();
                if(log.isDebugEnabled() && cachedValue instanceof ArrayList<?>){
                    ArrayList<?> list = (ArrayList<?>)cachedValue;
                    log.debug("injecting List with " + list.size() + " elements into:\t" + cacheCommand.getCacheID());
                }
                cacheCommand.injectCacheResult(cachedValue);
                return cacheCommand;
            } else {
                command = getCommandService().executeCommand((GenericCommand)command);
                cacheCommand = (ICachedCommand)command;

                Object cachedValue = cacheCommand.getCacheableResult();
                if(log.isDebugEnabled() && cachedValue instanceof ArrayList<?>){
                    ArrayList<?> list = (ArrayList<?>)cachedValue;
                    log.debug("putting List with " + list.size() + " elements from "  + cacheCommand.getCacheID() + " to Cache");
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
        if(log.isDebugEnabled()){
            log.debug("Global Report Cache created! Storing cache in:\t" + manager.getDiskStorePath());
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
    
    private void logCacheStatistics(){
        Statistics stats = getGlobalCache().getStatistics();
        log.debug("################start Cache Stats###################");
        log.debug("AvgGetTime:\t" + stats.getAverageGetTime());
        log.debug("CacheHits:\t" + stats.getCacheHits());
        log.debug("CacheMisses:\t" + stats.getCacheMisses());
        log.debug("DiskStoreObjectCount:\t" + stats.getDiskStoreObjectCount());
        log.debug("EvictionCount:\t" + stats.getEvictionCount());
        log.debug("InMemoryHits:\t" + stats.getInMemoryHits());
        log.debug("MemoryStoreObjectCount:\t" + stats.getMemoryStoreObjectCount());
        log.debug("ObjectCount:\t" + stats.getObjectCount());
        log.debug("OnDiskHits:\t" + stats.getOnDiskHits());
        log.debug("StatisticAccuracy:\t" + stats.getStatisticsAccuracy());
        log.debug("StatisticAccuracyDescription:\t" + stats.getStatisticsAccuracyDescription());
        log.debug("################end Cache Stats###################");
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommandCacheClient#setUseCache(boolean)
     */
    @Override
    public void setUseCache(boolean useCache) {
        
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommandService#configureFilter(sernet.verinice.interfaces.IBaseDao)
     */
    @Override
    public void configureFilter(IBaseDao dao) {  
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommandService#disableFilter(sernet.verinice.interfaces.IBaseDao)
     */
    @Override
    public void disableFilter(IBaseDao dao) {
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommandService#getVnaSchemaVersion()
     */
    @Override
    public VnaSchemaVersion getVnaSchemaVersion() {
        // delegate to server
        return commandService.getVnaSchemaVersion();
    }

}

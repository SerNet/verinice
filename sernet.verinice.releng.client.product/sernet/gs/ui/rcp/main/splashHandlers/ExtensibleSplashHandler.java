
package sernet.gs.ui.rcp.main.splashHandlers;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.splash.AbstractSplashHandler;

/**
 * @since 3.3
 *
 */
public class ExtensibleSplashHandler extends AbstractSplashHandler {
	
	private ArrayList fImageList;
	
	private ArrayList fTooltipList;

	private final static String F_SPLASH_EXTENSION_ID = "sernet.gs.ui.rcp.main.splashExtension"; //NON-NLS-1
	
	private final static String F_ELEMENT_ICON = "icon"; //NON-NLS-1
	
	private final static String F_ELEMENT_TOOLTIP = "tooltip"; //NON-NLS-1
	
	private final static String F_DEFAULT_TOOLTIP = "Image"; //NON-NLS-1
	
	private final static int F_IMAGE_WIDTH = 50;
	
	private final static int F_IMAGE_HEIGHT = 50;
	
	private final static int F_SPLASH_SCREEN_BEVEL = 5;
	
	private Composite fIconPanel;
	
	/**
	 * 
	 */
	public ExtensibleSplashHandler() {
		fImageList = new ArrayList();
		fTooltipList = new ArrayList();
		fIconPanel = null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.splash.AbstractSplashHandler#init(org.eclipse.swt.widgets.Shell)
	 */
	public void init(Shell splash) {
		// Store the shell
		super.init(splash);
		// Configure the shell layout
		configureUISplash();
		// Load all splash extensions
		loadSplashExtensions();
		// If no splash extensions were loaded abort the splash handler
		if (hasSplashExtensions() == false) {
			return;
		}
		// Create UI
		createUI();
		// Configure the image panel bounds
		configureUICompositeIconPanelBounds();
		// Enter event loop and prevent the RCP application from 
		// loading until all work is done
		doEventLoop();
	}

	/**
	 * @return
	 */
	private boolean hasSplashExtensions() {
		if (fImageList.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * 
	 */
	private void createUI() {
		// Create the icon panel
		createUICompositeIconPanel();
		// Create the images
		createUIImages();
	}	
	
	/**
	 * 
	 */
	private void createUIImages() {
		Iterator imageIterator = fImageList.iterator();
		Iterator tooltipIterator = fTooltipList.iterator();
		int i = 1;
		int columnCount = ((GridLayout)fIconPanel.getLayout()).numColumns;
		// Create all the images 
		// Abort if we run out of columns (left-over images will not fit within
		// the usable splash screen width)
		while (imageIterator.hasNext() && 
				(i <= columnCount)) {
			Image image = (Image)imageIterator.next();
			String tooltip = (String)tooltipIterator.next();
			// Create the image using a label widget
			createUILabel(image, tooltip);
			i++;
		}
	}
	
	/**
	 * @param image
	 * @param tooltip
	 */
	private void createUILabel(Image image, String tooltip) {
		// Create the label (no text)
		Label label = new Label(fIconPanel, SWT.NONE);
		label.setImage(image);
		label.setToolTipText(tooltip);		
	}

	/**
	 * 
	 */
	private void createUICompositeIconPanel() {
		Shell splash = getSplash();
		// Create the composite
		fIconPanel = new Composite(splash, SWT.NONE);
		// Determine the maximum number of columns that can fit on the splash
		// screen.  One 50x50 image per column.
		int maxColumnCount = getUsableSplashScreenWidth() / F_IMAGE_WIDTH;
		// Limit size to the maximum number of columns if the number of images
		// exceed this amount; otherwise, use the exact number of columns 
		// required.
		int actualColumnCount = Math.min(fImageList.size(), maxColumnCount);
		// Configure the layout
		GridLayout layout = new GridLayout(actualColumnCount, true);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		fIconPanel.setLayout(layout);
	}

	/**
	 * 
	 */
	private void configureUICompositeIconPanelBounds() {
		// Determine the size of the panel and position it at the bottom-right
		// of the splash screen.
		Point panelSize = fIconPanel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		
		int x_coord = getSplash().getSize().x - F_SPLASH_SCREEN_BEVEL - panelSize.x;
		int y_coord = getSplash().getSize().y - F_SPLASH_SCREEN_BEVEL - panelSize.y; 
		int x_width = panelSize.x;
		int y_width = panelSize.y;
		
		fIconPanel.setBounds(x_coord, y_coord, x_width, y_width);	
	}
	
	/**
	 * @return
	 */
	private int getUsableSplashScreenWidth() {
		// Splash screen width minus two graphic border bevel widths
		return getSplash().getSize().x - (F_SPLASH_SCREEN_BEVEL * 2);
	}
	
	/**
	 * 
	 */
	private void loadSplashExtensions() {
		// Get all splash handler extensions
		IExtension[] extensions = 
			Platform.getExtensionRegistry().getExtensionPoint(
					F_SPLASH_EXTENSION_ID).getExtensions();
		// Process all splash handler extensions
		for (int i = 0; i < extensions.length; i++) {
			processSplashExtension(extensions[i]);
		}
	}

	/**
	 * @param extension
	 */
	private void processSplashExtension(IExtension extension) {
		// Get all splash handler configuration elements
		IConfigurationElement[] elements = extension.getConfigurationElements();
		// Process all splash handler configuration elements
		for (int j = 0; j < elements.length; j++) {
			processSplashElements(elements[j]);
		}
	}

	/**
	 * @param configurationElement
	 */
	private void processSplashElements(
			IConfigurationElement configurationElement) {
		// Attribute: icon
		processSplashElementIcon(configurationElement);
		// Attribute: tooltip
		processSplashElementTooltip(configurationElement);
	}

	/**
	 * @param configurationElement
	 */
	private void processSplashElementTooltip(
			IConfigurationElement configurationElement) {
		// Get attribute tooltip
		String tooltip = configurationElement.getAttribute(F_ELEMENT_TOOLTIP);
		// If a tooltip is not defined, give it a default
		if ((tooltip == null) || 
				(tooltip.length() == 0)) {
			fTooltipList.add(F_DEFAULT_TOOLTIP);
		} else {
			fTooltipList.add(tooltip);
		}
	}

	/**
	 * @param configurationElement
	 */
	private void processSplashElementIcon(
			IConfigurationElement configurationElement) {
		// Get attribute icon
		String iconImageFilePath = configurationElement.getAttribute(F_ELEMENT_ICON);
		// Abort if an icon attribute was not specified
		if ((iconImageFilePath == null) ||
				(iconImageFilePath.length() == 0)) {
			return;
		}
		// Create a corresponding image descriptor
		ImageDescriptor descriptor = 
			AbstractUIPlugin.imageDescriptorFromPlugin(
					configurationElement.getNamespaceIdentifier(), 
					iconImageFilePath);
		// Abort if no corresponding image was found
		if (descriptor == null) {
			return;
		}
		// Create the image
		Image image = descriptor.createImage();
		// Abort if image creation failed
		if (image == null) {
			return;
		}
		// Abort if the image does not have dimensions of 50x50
		if ((image.getBounds().width != F_IMAGE_WIDTH) || 
				(image.getBounds().height != F_IMAGE_HEIGHT)) {
			// Dipose of the image
			image.dispose();
			return;
		}
		// Store the image and tooltip
		fImageList.add(image);
	}

	/**
	 * 
	 */
	private void configureUISplash() {
		// Configure layout
		GridLayout layout = new GridLayout(1, true);
		getSplash().setLayout(layout);
		// Force shell to inherit the splash background
		getSplash().setBackgroundMode(SWT.INHERIT_DEFAULT);		
	}	
	
	/**
	 * 
	 */
	private void doEventLoop() {
		Shell splash = getSplash();
		if (splash.getDisplay().readAndDispatch() == false) {
			splash.getDisplay().sleep();
		}
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.splash.AbstractSplashHandler#dispose()
	 */
	public void dispose() {
		super.dispose();
		// Check to see if any images were defined
		if ((fImageList == null) ||
				fImageList.isEmpty()) {
			return;
		}
		// Dispose of all the images
		Iterator iterator = fImageList.iterator();
		while (iterator.hasNext()) {
			Image image = (Image) iterator.next();
			image.dispose();
		}
	}
}

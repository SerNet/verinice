package org.primefaces.poseidon.component.menu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.primefaces.component.api.AjaxSource;
import org.primefaces.component.api.UIOutcomeTarget;
import org.primefaces.component.menu.AbstractMenu;
import org.primefaces.component.menu.BaseMenuRenderer;
import org.primefaces.model.menu.MenuElement;
import org.primefaces.model.menu.MenuItem;
import org.primefaces.model.menu.Separator;
import org.primefaces.model.menu.Submenu;
import org.primefaces.util.ComponentUtils;
import org.primefaces.util.WidgetBuilder;

public class PoseidonMenuRenderer extends BaseMenuRenderer {

    @Override
    protected void encodeMarkup(FacesContext context, AbstractMenu abstractMenu)
            throws IOException {
        PoseidonMenu menu = (PoseidonMenu) abstractMenu;
        ResponseWriter writer = context.getResponseWriter();
        String style = menu.getStyle();
        String styleClass = menu.getStyleClass();
        styleClass = (styleClass == null) ? "layout-menu clearfix"
                : "layout-menu clearfix" + styleClass;

        writer.startElement("ul", menu);
        writer.writeAttribute("id", menu.getClientId(context), "id");
        writer.writeAttribute("class", styleClass, "styleClass");
        if (style != null)
            writer.writeAttribute("style", style, "style");

        if (menu.getElementsCount() > 0) {
            encodeElements(context, menu, menu.getElements());
        }

        writer.endElement("ul");
    }

    protected void encodeElements(FacesContext context, AbstractMenu menu,
            List<MenuElement> elements) throws IOException {
        for (MenuElement element: elements) {
            encodeElement(context, menu, element);
        }
    }

    protected void encodeElement(FacesContext context, AbstractMenu menu, MenuElement element)
            throws IOException {
        if (!element.isRendered()) {
            return;
        }
        ResponseWriter writer = context.getResponseWriter();
        if (element instanceof MenuItem) {
            MenuItem menuItem = (MenuItem) element;
            String menuItemClientId = (menuItem instanceof UIComponent) ? menuItem.getClientId()
                    : menu.getClientId(context) + "_" + menuItem.getClientId();
            String containerStyle = menuItem.getContainerStyle();
            String containerStyleClass = menuItem.getContainerStyleClass();

            renderListItem(writer, menuItemClientId, containerStyle, containerStyleClass);

            encodeMenuItem(context, menu, menuItem);

            writer.endElement("li");
        } else if (element instanceof Submenu) {
            Submenu submenu = (Submenu) element;
            String submenuClientId = (submenu instanceof UIComponent)
                    ? ((UIComponent) submenu).getClientId()
                    : menu.getClientId(context) + "_" + submenu.getId();
            String style = submenu.getStyle();
            String styleClass = submenu.getStyleClass();

            renderListItem(writer, submenuClientId, style, styleClass);

            encodeSubmenu(context, menu, submenu);

            writer.endElement("li");
        } else if (element instanceof Separator) {
            encodeSeparator(context, (Separator) element);
        }

    }

    private static void renderListItem(ResponseWriter writer, String id, String elementStyle,
            String elementClass) throws IOException {
        writer.startElement("li", null);
        writer.writeAttribute("id", id, null);
        writer.writeAttribute("role", "menuitem", null);
        if (elementStyle != null)
            writer.writeAttribute("style", elementStyle, null);
        if (elementClass != null)
            writer.writeAttribute("class", elementClass, null);
    }

    protected void encodeSubmenu(FacesContext context, AbstractMenu menu, Submenu submenu)
            throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String icon = submenu.getIcon();
        String label = submenu.getLabel();
        int childrenElementsCount = submenu.getElementsCount();

        writer.startElement("a", null);
        writer.writeAttribute("href", "#", null);
        writer.writeAttribute("class", "ripplelink", null);

        encodeItemIcon(context, icon);

        if (label != null) {
            writer.startElement("span", null);
            writer.writeText(label, null);
            writer.endElement("span");

            writer.startElement("span", null);
            writer.writeAttribute("class", "ink animate", null);
            writer.endElement("span");

            encodeToggleIcon(context, submenu, childrenElementsCount);
        }

        writer.endElement("a");

        // submenus and menuitems
        if (childrenElementsCount > 0) {
            writer.startElement("ul", null);
            writer.writeAttribute("role", "menu", null);
            encodeElements(context, menu, submenu.getElements());
            writer.endElement("ul");
        }
    }

    protected void encodeItemIcon(FacesContext context, String icon) throws IOException {
        if (icon != null) {
            ResponseWriter writer = context.getResponseWriter();

            writer.startElement("i", null);
            writer.writeAttribute("class", icon, null);
            writer.endElement("i");
        }
    }

    protected void encodeToggleIcon(FacesContext context, Submenu submenu,
            int childrenElementsCount) throws IOException {
        if (childrenElementsCount > 0) {
            ResponseWriter writer = context.getResponseWriter();

            writer.startElement("i", null);
            writer.writeAttribute("class", "fa fa-fw fa-angle-down", null);
            writer.endElement("i");
        }
    }

    @Override
    protected void encodeSeparator(FacesContext context, Separator separator) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String style = separator.getStyle();
        String styleClass = separator.getStyleClass();
        styleClass = styleClass == null ? "Separator" : "Separator " + styleClass;

        // title
        writer.startElement("li", null);
        writer.writeAttribute("class", styleClass, null);
        if (style != null) {
            writer.writeAttribute("style", style, null);
        }

        writer.endElement("li");
    }

    @Override
    protected void encodeMenuItem(FacesContext context, AbstractMenu menu, MenuItem menuitem)
            throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String title = menuitem.getTitle();
        boolean disabled = menuitem.isDisabled();
        String style = menuitem.getStyle();
        String styleClass = menuitem.getStyleClass();

        writer.startElement("a", null);
        if (title != null)
            writer.writeAttribute("title", title, null);
        if (style != null)
            writer.writeAttribute("style", style, null);
        if (styleClass != null)
            writer.writeAttribute("class", styleClass, null);

        if (disabled) {
            writer.writeAttribute("href", "#", null);
            writer.writeAttribute("onclick", "return false;", null);
        } else {
            String onclick = menuitem.getOnclick();

            // GET
            if (menuitem.getUrl() != null || menuitem.getOutcome() != null) {
                String targetURL = getTargetURL(context, (UIOutcomeTarget) menuitem);
                writer.writeAttribute("href", targetURL, null);

                if (menuitem.getTarget() != null) {
                    writer.writeAttribute("target", menuitem.getTarget(), null);
                }
            }
            // POST
            else {
                writer.writeAttribute("href", "#", null);

                UIComponent form = ComponentUtils.findParentForm(context, menu);
                if (form == null) {
                    throw new FacesException("MenuItem must be inside a form element");
                }

                String command;
                if (menuitem.isDynamic()) {
                    String menuClientId = menu.getClientId(context);
                    Map<String, List<String>> params = menuitem.getParams();
                    if (params == null) {
                        params = new LinkedHashMap<>(1);
                    }
                    List<String> idParams = new ArrayList<>(1);
                    idParams.add(menuitem.getId());
                    params.put(menuClientId + "_menuid", idParams);

                    command = menuitem.isAjax()
                            ? buildAjaxRequest(context, menu, (AjaxSource) menuitem, form, params)
                            : buildNonAjaxRequest(context, menu, form, menuClientId, params, true);
                } else {
                    command = menuitem.isAjax()
                            ? buildAjaxRequest(context, (AjaxSource) menuitem, form)
                            : buildNonAjaxRequest(context, ((UIComponent) menuitem), form,
                                    ((UIComponent) menuitem).getClientId(context), true);
                }

                onclick = (onclick == null) ? command : onclick + ";" + command;
            }

            if (onclick != null) {
                writer.writeAttribute("onclick", onclick, null);
            }
        }

        encodeMenuItemContent(context, menu, menuitem);

        writer.endElement("a");
    }

    @Override
    protected void encodeMenuItemContent(FacesContext context, AbstractMenu menu, MenuItem menuitem)
            throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String icon = menuitem.getIcon();
        Object value = menuitem.getValue();

        encodeItemIcon(context, icon);

        if (value != null) {
            writer.startElement("span", null);
            writer.writeText(value, "value");
            writer.endElement("span");
        }
    }

    @Override
    protected void encodeScript(FacesContext context, AbstractMenu abstractMenu)
            throws IOException {
        PoseidonMenu menu = (PoseidonMenu) abstractMenu;
        String clientId = menu.getClientId(context);
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("Poseidon", menu.resolveWidgetVar(), clientId).finish();
    }

}

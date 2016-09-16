package org.baddev.currency.ui.security;

import com.vaadin.spring.access.ViewAccessControl;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.UI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.security.DeclareRoles;

import static org.baddev.currency.security.utils.SecurityCtxHelper.hasAnyRole;
import static org.baddev.currency.security.utils.SecurityCtxHelper.isLoggedIn;

/**
 * Created by IPotapchuk on 7/1/2016.
 */
@Component
public class RoleBasedViewAccessControl implements ViewAccessControl {

    @Autowired
    private ApplicationContext ctx;

    @Override
    public boolean isAccessGranted(UI ui, String beanName) {
        if (ctx.findAnnotationOnBean(beanName, SpringView.class) == null)
            return false;
        final DeclareRoles rolesAnot = ctx.findAnnotationOnBean(beanName, DeclareRoles.class);
        return rolesAnot == null || isLoggedIn() && hasAnyRole(rolesAnot.value());
    }
}

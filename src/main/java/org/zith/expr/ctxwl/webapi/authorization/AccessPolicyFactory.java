package org.zith.expr.ctxwl.webapi.authorization;

import org.glassfish.hk2.api.Factory;
import org.zith.expr.ctxwl.core.accesscontrol.AccessPolicy;
import org.zith.expr.ctxwl.core.accesscontrol.ClassPrefixedRoleCodec;
import org.zith.expr.ctxwl.core.accesscontrol.RoleAssigner;
import org.zith.expr.ctxwl.core.identity.ControlledResourceType;
import org.zith.expr.ctxwl.webapi.authorization.role.EmailRegistrantRole;

import java.util.List;
import java.util.Objects;

public class AccessPolicyFactory implements Factory<AccessPolicy> {
    @Override
    public AccessPolicy provide() {
        return AccessPolicy.of(
                List.of(
                        RoleAssigner.forClass(EmailRegistrantRole.class, (principal, role) ->
                                principal.resourceIdentifier().type() == ControlledResourceType.EMAIL_REGISTRATION &&
                                        Objects.equals(principal.resourceIdentifier().identifier(), role.emailRegistrationIdentifier()))),
                ClassPrefixedRoleCodec.of(List.of(
                        ClassPrefixedRoleCodec.prefix(
                                EmailRegistrantRole.class,
                                "email-registrant",
                                EmailRegistrantRole::emailRegistrationIdentifier,
                                EmailRegistrantRole::new)
                ))
        );
    }

    @Override
    public void dispose(AccessPolicy accessPolicy) {

    }
}

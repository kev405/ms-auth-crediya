package co.com.crediya.r2dbc.role;

import co.com.crediya.model.role.Role;
import co.com.crediya.model.role.gateways.RoleRepository;
import co.com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import co.com.crediya.r2dbc.role.entity.RoleEntity;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;

@Repository
public class RoleReactiveRepositoryAdapter extends ReactiveAdapterOperations<
    Role,
        RoleEntity,
    String,
    RoleReactiveRepository
> implements RoleRepository {


    public RoleReactiveRepositoryAdapter(RoleReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, entity -> mapper.map(entity, Role.class));
    }
}

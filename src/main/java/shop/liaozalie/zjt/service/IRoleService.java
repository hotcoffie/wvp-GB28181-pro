package shop.liaozalie.zjt.service;

import shop.liaozalie.zjt.storager.dao.dto.Role;

import java.util.List;

public interface IRoleService {

    Role getRoleById(int id);

    int add(Role role);

    int delete(int id);

    List<Role> getAll();

    int update(Role role);
}

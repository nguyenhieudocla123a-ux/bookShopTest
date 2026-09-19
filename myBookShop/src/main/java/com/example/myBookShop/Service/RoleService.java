package com.example.myBookShop.Service;

import com.example.myBookShop.Controllers.RoleController;
import com.example.myBookShop.DTO.Respone.RoleRespone;
import com.example.myBookShop.Entity.Role;
import com.example.myBookShop.Exception.ConflictDataException;
import com.example.myBookShop.Exception.ResourceNotFoundException;
import com.example.myBookShop.Mapper.RoleMapper;
import com.example.myBookShop.Repositories.RoleRepository;
import com.example.myBookShop.Repositories.UserRoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final UserRoleRepository userRoleRepository;
    //Get all role
    public List<RoleRespone> getAll(){
        return roleRepository.findAll().stream().map(roleMapper::toResponse).toList();
    }

    //Add new role
    @Transactional
    public  RoleRespone create(String roleName){
        if(roleRepository.existsByName(roleName)) throw  new ConflictDataException("Role is existed");
        Role role= new Role();
        role.setName(roleName);
        roleRepository.save(role);
        return roleMapper.toResponse(role);
    }
    //Update name if role hasn't had a relation with others
    @Transactional
    public void updateName(String name,String newName){
        Role updateRole=roleRepository.findByName(name).orElseThrow(()->new ResourceNotFoundException("Role isn't existed"));
        if(userRoleRepository.existsByRole(updateRole)) throw  new ConflictDataException("Some user currently has had relations with this role!");
        updateRole.setName(newName);
    }
    //DeleteRole
    @Transactional
    public void delete(String name){
        Role role=roleRepository.findByName(name).orElseThrow(()->new ResourceNotFoundException("Role isn't existed"));
        roleRepository.deleteByName(role.getName());
    }
}

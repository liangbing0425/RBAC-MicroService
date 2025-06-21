package org.shuzimali.permissionservice.service.impl;

import org.shuzimali.permissionservice.dto.PermissionDTO;
import org.shuzimali.permissionservice.entity.Permission;
import org.shuzimali.permissionservice.mapper.PermissionMapper;
import org.shuzimali.permissionservice.service.PermissionService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionMapper permissionMapper;

    @Override
    @Transactional
    public PermissionDTO createPermission(PermissionDTO permissionDTO) {
        Permission permission = new Permission();
        BeanUtils.copyProperties(permissionDTO, permission);
        permissionMapper.insert(permission);
        permissionDTO.setId(permission.getId());
        return permissionDTO;
    }

    @Override
    @Transactional
    public PermissionDTO updatePermission(PermissionDTO permissionDTO) {
        Permission permission = new Permission();
        BeanUtils.copyProperties(permissionDTO, permission);
        permissionMapper.update(permission);
        return permissionDTO;
    }

    @Override
    @Transactional
    public void deletePermission(Long id) {
        permissionMapper.delete(id);
    }

    @Override
    public PermissionDTO getPermissionById(Long id) {
        Permission permission = permissionMapper.selectById(id);
        return convertToDTO(permission);
    }

    @Override
    public PermissionDTO getPermissionByCode(String code) {
        Permission permission = permissionMapper.selectByCode(code);
        return convertToDTO(permission);
    }

    @Override
    public List<PermissionDTO> getAllPermissions() {
        List<Permission> permissions = permissionMapper.selectAll();
        return permissions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private PermissionDTO convertToDTO(Permission permission) {
        if (permission == null) {
            return null;
        }
        PermissionDTO permissionDTO = new PermissionDTO();
        BeanUtils.copyProperties(permission, permissionDTO);
        return permissionDTO;
    }
}

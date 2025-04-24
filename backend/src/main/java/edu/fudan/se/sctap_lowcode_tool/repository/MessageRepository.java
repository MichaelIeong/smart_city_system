package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.MessageInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessageInfo, Integer> {

    // 按 uuid 排序并排除系统消息
    @Query("SELECT m FROM MessageInfo m WHERE m.uuid = :uuid AND m.messageType != 2 ORDER BY m.id ASC")
    List<MessageInfo> findMessagesByUuidExcludingSystemMessages(String uuid);

    // 按 uuid 排序，不排除任何消息类型
    @Query("SELECT m FROM MessageInfo m WHERE m.uuid = :uuid ORDER BY m.id ASC")
    List<MessageInfo> findAllMessagesByUuidOrderedById(String uuid);

    // 根据 uuid 查询数据库中已有的消息数量
    long countByUuid(String uuid);
}

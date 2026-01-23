package edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan;

import edu.fudan.se.sctap_lowcode_tool.DTO.BadRequestException;
import edu.fudan.se.sctap_lowcode_tool.DTO.PageDTO;
import edu.fudan.se.sctap_lowcode_tool.model.event_fusion_2026_jan.EventFusionRunHistory;
import edu.fudan.se.sctap_lowcode_tool.repository.EventFusionRunHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <h3>EventFusionRunHistoryService 事件融合运行历史服务</h3>
 * 负责事件融合规则执行历史的查询。
 * @author Lin Yicheng
 * @since 2026-01-22
 */
@Service
@RequiredArgsConstructor
public class EventFusionRunHistoryService {
    private final EventFusionRunHistoryRepository runHistoryRepository;

    /**
     * BriefResponse 运行历史简要响应
     * @param id 运行历史记录ID
     * @param ruleName 规则名称
     * @param success 执行是否成功
     * @param createTime 创建时间
     */
    public record BriefResponse(
        Integer id,
        String ruleName,
        Boolean success,
        String createTime
    ) {
        public static BriefResponse of(EventFusionRunHistory entity) {
            return new BriefResponse(
                entity.getId(),
                entity.getRuleName(),
                entity.getIsSuccess(),
                entity.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );
        }
    }

    /**
     * 分页获取运行历史记录
     *
     * @param pageNum 页码（从 0 开始）
     * @param pageSize 每页数量
     * @return 分页结果，包含简要响应列表
     */
    public PageDTO<BriefResponse> getRunHistories(int pageNum, int pageSize) {
        var page = runHistoryRepository.findAll(
            PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return new PageDTO<>(
            pageNum, pageSize,
            page.getTotalElements(),
            page.getTotalPages(),
            page.getContent().stream().map(BriefResponse::of).toList()
        );
    }

    /**
     * 获取运行历史详情
     *
     * @param id 运行历史记录ID
     * @return 运行历史详细信息
     * @throws BadRequestException 当指定 ID 的记录不存在时抛出
     */
    public EventFusionRunHistory getRunHistoryDetail(Integer id) {
        return runHistoryRepository.findById(id).orElseThrow(
            () -> new BadRequestException("400", "ID为 " + id + " 的运行历史记录不存在", List.of())
        );
    }

}

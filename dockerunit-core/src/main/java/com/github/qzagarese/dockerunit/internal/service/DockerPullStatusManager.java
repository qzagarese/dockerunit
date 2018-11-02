package com.github.qzagarese.dockerunit.internal.service;

import java.util.HashMap;
import java.util.Map;

import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.api.model.ResponseItem.ProgressDetail;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DockerPullStatusManager {
    
    private final String imageName;
    
    private Map<String, ItemStatus> statuses = new HashMap<>();
    
    public String update(PullResponseItem item) {
        int previousVerticalBufferSize = statuses.size();
        if (item != null && item.getId() != null) {
            statuses.put(item.getId(), createStatusItem(item, statuses.get(item.getId())));
        }
        return computeStatus(previousVerticalBufferSize);
    }
    
    private ItemStatus createStatusItem(PullResponseItem item, ItemStatus previousStatus) {
        ProgressDetail progressDetail = item.getProgressDetail();
        if (progressDetail != null && progressDetail.getTotal() != null  && progressDetail.getTotal() > 0) {
            int currentPct = computePercentage(previousStatus, progressDetail);
            
            return ItemStatus.builder()
                    .id(item.getId())
                    .percentage(currentPct)
                    .pullStatus(statusNotNull(item.getStatus())? item.getStatus() : "")
                    .build();
        }
        return ItemStatus.builder()
                .id(item.getId())
                .percentage(0)
                .pullStatus(statusNotNull(item.getStatus())? item.getStatus() : "")
                .build();
    }

    private int computePercentage(ItemStatus previousStatus, ProgressDetail progressDetail) {
        int currentPct = (int)(
                ((float) progressDetail.getCurrent()) / 
                ((float) progressDetail.getTotal()) 
                * 100);
        currentPct = (previousStatus != null && previousStatus.getPercentage() > currentPct) ? 
                        previousStatus.getPercentage() 
                    : 
                        currentPct;
        return currentPct;
    }

    private boolean statusNotNull(String status) {
        return status != null && !status.trim().equals("null");
    }

    private String computeStatus(int previousVerticalBufferSize) {
        StringBuffer status = new StringBuffer();
        status.append("\rPulling image ")
                .append(imageName);
        for (int i = 0; i < previousVerticalBufferSize; i++) {
            status.append("\033[F")
                .append("                                                                                              ")
                .append("\r");
        }
        statuses.values().forEach(item -> status
                .append("\r")
                .append(item.toString())
                .append("\n"));
        return status.toString();
    }

    @Builder
    @Getter
    static class ItemStatus{
        
        private static final int PROGRESS_BAR_STRING_LENGTH = 20;
        
        private String id;
        private int percentage; 
        private String pullStatus;
        
        public String toString() {
            StringBuffer buffer = new StringBuffer()
                    .append(id)
                    .append("\t\t");
            if (percentage > 0) { 
                    buffer.append(computePctString())
                        .append(" ")
                        .append("[")
                        .append(computeProgressBar())
                        .append("]")
                        .append(" ");
            }
            return buffer
                    .append(pullStatus)
                    .toString();        
        }

        private String computeProgressBar() {
            StringBuffer progress = new StringBuffer();
            for(int i = 1; i <= PROGRESS_BAR_STRING_LENGTH; i++) {
                if(percentage >= (100 / PROGRESS_BAR_STRING_LENGTH * (i))) {
                    progress.append("=");
                } else {
                    progress.append(" ");
                }
            }
            return progress.toString();
        }

        private String computePctString() {
            return  (percentage < 100 ? " " : "")
                    + (percentage < 10 ? " " : "")
                    + percentage
                    + "%";
        }
        
    }
}

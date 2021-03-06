package com.example.candy.controller.candyHistory;

import com.example.candy.controller.ApiResult;
import com.example.candy.controller.candyHistory.dto.*;
import com.example.candy.domain.candy.CandyHistory;
import com.example.candy.domain.candy.CandyType;
import com.example.candy.security.JwtAuthentication;
import com.example.candy.service.candyHistory.CandyHistoryService;
import com.example.candy.service.challenge.ChallengeLikeService;
import com.example.candy.service.challenge.ChallengeService;
import io.swagger.annotations.*;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/candy")
@RequiredArgsConstructor
@Api(tags = {"캔디"})
public class CandyController {

    private final CandyHistoryService candyHistoryService;
    private final ChallengeLikeService challengeLikeService;

    @GetMapping("/{identity}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "identity", value = "학생 조회 시 student \n 부모 조회 시 parent")
    })
    @ApiOperation(value = "학생 or 학부모 캔디 보유 수 조회")
    public ApiResult<CandyResponseDto> candyStudent(@AuthenticationPrincipal JwtAuthentication authentication,
                                                    @PathVariable @ApiParam String identity) {
        return ApiResult.OK(new CandyResponseDto(candyHistoryService.findCandyAmount(authentication.id, identity)));
    }

    @PostMapping("/charge")
    @ApiOperation(value = "캔디 충전")
    public ApiResult<CandyResponseDto> chargeCandy(@AuthenticationPrincipal JwtAuthentication authentication,
            @RequestBody @ApiParam CandyChargeRequestDto candyChargeRequestDto) {
        return ApiResult.OK(new CandyResponseDto(
                candyHistoryService.chargeCandy(authentication.id,
                candyChargeRequestDto.getAmount()).getParentCandy()));
    }

    @PostMapping("/withdraw")
    @ApiOperation(value = "캔디 인출")
    public ApiResult<CandyWithdrawResponseDto> withdrawCandy(@AuthenticationPrincipal JwtAuthentication authentication,
                                                             @RequestBody @ApiParam CandyWithdrawRequestDto candyWithdrawRequestDto) {
        return ApiResult.OK(new CandyWithdrawResponseDto(
                candyHistoryService.withdrawCandy(authentication.id, candyWithdrawRequestDto.getAmount()).getStudentCandy(),
                candyWithdrawRequestDto.getAmount())
        );
    }

    @PostMapping("/assign")
    @ApiOperation(value = "캔디 배정 (부모 캔디를 챌린지에 배정)")
    public ApiResult assignCandy(@AuthenticationPrincipal JwtAuthentication authentication,
                                 @RequestBody @ApiParam CandyAssignRequestDto candyAssignRequestDto) throws NotFoundException {
        CandyHistory candyHistory = candyHistoryService.assignCandy(authentication.id, candyAssignRequestDto.getParentPassword(),
                candyAssignRequestDto.getChallengeId(), candyAssignRequestDto.getCandyAmount());
        // 캔디 배정하는 순간 찜한 목록에서 제거
        challengeLikeService.delete(authentication.id, candyAssignRequestDto.getChallengeId());
        return ApiResult.OK(candyHistory);
    }

    @PostMapping("/cancel")
    @ApiOperation(value = "캔디 배정 취소 (챌린지에 배정된 캔디 취소)")
    public ApiResult cancelCandy(@AuthenticationPrincipal JwtAuthentication authentication,
                                 @RequestBody @ApiParam CandyCancelRequestDto candyCancelRequestDto) throws NotFoundException {
        CandyHistory candyHistory = candyHistoryService.cancelCandy(authentication.id,
                candyCancelRequestDto.getParentPassword(), candyCancelRequestDto.getChallengeId());
        return ApiResult.OK(candyHistory);
    }

    @PostMapping("/attain")
    @ApiOperation(value = "캔디 획득 (챌린지 성공 후 챌린지에 배정된 캔디 -> 학생 캔디로 획득), 챌린지 성공 처리도 같이 함")
    public ApiResult attainCandy(@AuthenticationPrincipal JwtAuthentication authentication,
                                 @RequestBody @ApiParam CandyAttainRequestDto candyAttainRequestDto) {
        candyHistoryService.attainCandy(authentication.id, candyAttainRequestDto.getChallengeId());
        return ApiResult.OK(null);
    }

    @GetMapping("/history/{identity}/{category}/{lastCandyHistoryId}/{size}")
    @ApiOperation(value = "학생 관련 모든 캔디 내역 리스트 불러오기")
        @ApiImplicitParams({
                @ApiImplicitParam(name = "identity", value = "학생 조회 시 student \n 부모 조회 시 parent"),
                @ApiImplicitParam(name = "category", value = "학생 조회 시 all / attain(습득) / withdraw(인출) \n" +
                        "부모 조회 시 all / charge(충전) / assign(배정) / cancel(배정 취소)"),
                @ApiImplicitParam(name = "lastCandyHistoryId", value = "이미 조회 된 candyHistory 데이터 중 가장 마지막 개체의 id"),
                @ApiImplicitParam(name = "size", value = "불러올 데이터 갯수")
        })
    public ApiResult<List<CandyHistoryResponseDto>> getCandyHistory(@AuthenticationPrincipal JwtAuthentication authentication,
                                                                       @PathVariable String identity, @PathVariable String category,
                                                                       @PathVariable Long lastCandyHistoryId, @PathVariable int size) {
        List<CandyHistoryResponseDto> candyHistoryResponseDtoList = candyHistoryService.getCandyHistory(authentication.id, identity, category, lastCandyHistoryId, size);
        return ApiResult.OK(
                candyHistoryResponseDtoList
        );
    }
}

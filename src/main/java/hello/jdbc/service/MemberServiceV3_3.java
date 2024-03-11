package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * V3-2 트랜잭션 - 트랜잭션 템플릿 -> jdbc 생성자 주입은 트랜잭션 매니저로 해결
 * -> but 트랜잭션이 시작하고 비즈니스 로직을 실행할 때에 commit, rollback에 대한 try, catch문이 너무 자주 반복됨.
 * 따라서 transactionTemplate를 사용.
 *
 * V3-3 -> but, transactionTemplate를 사용했음에도 여전히 서비스계층에서 비즈니스 로직을 제외한 트랜잭션 코드가 존재
 * 따라서 스프링 AOP중 하나인 트랜잭션 AOP를 사용한다. = @Transactional (메서드앞 or 클래스(public 붙은 모든 메서드 적용))
 *
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_3 {

    private final MemberRepositoryV3 memberRepository;

    @Transactional
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        bizLogic(fromId, toId, money);
        bizLogic(fromId, toId, money);
        /*
        txTemplate.executeWithoutResult((status) -> {
            try {
                bizLogic(fromId, toId, money);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        });
        */
    }
    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById( fromId);
        Member toMember = memberRepository.findById(toId);
        memberRepository.update(fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(toId, toMember.getMoney() + money);
    }
    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }
}
package sample.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sample.form.PageCondition;
import sample.mapper.BulletinBoardDataExMapper;
import sample.mapper.BulletinBoardDataMapper;
import sample.mapper.SystemPropertyExMapper;
import sample.model.BulletinBoardData;
import sample.model.BulletinBoardDataExample;
import sample.model.SystemProperty;

@Service
public class BulletinBoardDataService {

    @Autowired
    private BulletinBoardDataMapper bulletinBoardDataMapper;

    @Autowired
    private BulletinBoardDataExMapper bulletinBoardDataExMapper;

    @Autowired
    private SystemPropertyExMapper systemPropertyExMapper;

    /**
     * 全件取得して返却する
     *
     * 全件取得のため条件は指定せずに検索する
     * ※わかりづらそうだからSQL文書くような形式にしたほうがいいかもしれない
     *
     * @return　List<BulletinBoardData>　検索結果
     */
    public List<BulletinBoardData> getBulletinBoardDataAll() {
        BulletinBoardDataExample exp = new BulletinBoardDataExample();
        return bulletinBoardDataMapper.selectByExample(exp);
    }

    /**
     * 名前で検索した結果を取得して返却する
     *
     * @return　List<BulletinBoardData>　検索結果
     */
    public List<BulletinBoardData> getSearchNameBulletinBoardData(String name, PageCondition pageCondition) {
        return bulletinBoardDataExMapper.selectByName(name, pageCondition.getStartingPosition(), pageCondition.getLimitCount());
    }

    /**
     * 名前で検索した結果を取得して返却する
     *
     * @return　List<BulletinBoardData>　検索結果
     */
    public List<BulletinBoardData> getAssignPageData(PageCondition pageCondition) {
        return bulletinBoardDataExMapper.getAssignPageData(pageCondition.getStartingPosition(), pageCondition.getLimitCount());
    }

    /**
     * １件のデータを登録する
     *
     * @return int　件数
     */
    @Transactional(rollbackFor = Exception.class)
    public int addBulletinBoardData(BulletinBoardData record) {
        Integer maxId = bulletinBoardDataExMapper.selectByIdMax();

        int id = 1;
        if (maxId != null) {
            id = maxId.intValue() + 1;
        }

        record.setId(id);
        return bulletinBoardDataMapper.insert(record);
    }

    /**
     * ページ情報を取得する
     *
     */
    public PageCondition getPageCondition(int page) {
        // システム設定値から画面表示件数取得
        SystemProperty systemProperty = getSystemProperty("DISPLAY_NUMBER");
        int limitCount = systemProperty != null ? Integer.parseInt(systemProperty.getValue()) : 0;
        // システム設定値からページ表示件数取得
        systemProperty = getSystemProperty("DISPLAY_PAGE_NUMBER");
        int displayPage = systemProperty != null ? Integer.parseInt(systemProperty.getValue()) : 0;
        // システム設定値からページング時の遷移先URL取得
        systemProperty = getSystemProperty("PAGING_TRANSITION_URL");
        String pageURL = systemProperty != null ? systemProperty.getValue() : null;

        // 総件数取得
        int totalCount = getBulletinBoardDataAll().size();
        // 開始位置の算出
        int startingPosition = page != 1 ? (page - 1) * limitCount - 1 : 0;
        // 終了位置の算出
        int endPosition = startingPosition + limitCount > totalCount ? totalCount : startingPosition + limitCount;
        // ページ数の算出
        int totalPage = (totalCount + limitCount) / limitCount;

        // ページ情報設定
        PageCondition pageCondition = new PageCondition();
        pageCondition.setTotalCount(totalCount);
        pageCondition.setLimitCount(limitCount);
        pageCondition.setPage(page);
        pageCondition.setStartingPosition(startingPosition);
        pageCondition.setEndPosition(endPosition);
        pageCondition.setTotalCount(totalCount);
        pageCondition.setTotalPage(totalPage);
        pageCondition.setDisplayPage(displayPage);
        pageCondition.setPageURL(pageURL);

        if (page > displayPage / 2) {
            // 最初に表示するページ番号を算出
            int startPageNumber = page - displayPage / 2 + 1;
            // ページの表示数をページ表示件数分必ず表示させるために最初に表示するページ数を調整
            if (totalPage < startPageNumber + displayPage) {
                startPageNumber = totalPage - displayPage + 1;
            }

            // 表示するページ番号のリストを作成
            List<Integer> pageNumberList = new ArrayList<Integer>();
            for (int i = startPageNumber; i < startPageNumber + displayPage; i++) {
                pageNumberList.add(i);
            }
            pageCondition.setPageNumberList(pageNumberList);
        }

        return pageCondition;
    }

    /**
     * システム設定値を取得する
     *
     * <pre>
     * キーが送られてきていない、または存在しないキーが送られてきた場合は
     * nullを返却する
     * </pre>
     *
     * @param key 検索キー
     * @return 検索結果
     */
    public SystemProperty getSystemProperty(String key) {

        if (key != null) {
            return systemPropertyExMapper.selectByKey(key);
        }

        return null;
    }
}

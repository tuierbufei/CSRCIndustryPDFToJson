package csrcIndustry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.giaybac.traprange.PDFTableExtractor;
import com.giaybac.traprange.entity.Table;

import flexjson.JSONSerializer;

public class Program {

	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException {

		PDFTableExtractor extractor = (new PDFTableExtractor()).setSource("resource/companyIndustry_201603.pdf");

		extractor.exceptLine(new int[] { 0 });
		List<Table> tables = extractor.extract();

		Map<String, Map<String, Map<String, String>>> industryHashMap = new LinkedHashMap<String, Map<String, Map<String, String>>>();
		String currentIndustryName = null;
		Map<String, Map<String, String>> currentIndustryMap = null;
		Map<String, String> currentCompanyIndustry = null;
		boolean previousIndustry = false;
		String content;
		Set<String> industriesIndex = new HashSet<String>();
		for (int i = 0; i < tables.size(); i++) {
			Table table = tables.get(i);
			for (int j = 0; j < table.getRows().size(); j++) {
				if (i == 0 && j == 0) {
					continue;
				}

				for (int k = 0; k < table.getRows().get(j).getCells().size(); k++) {
					content = table.getRows().get(j).getCells().get(k).getContent();
					if (!content.equals("")) {
						if (k == 0 && !previousIndustry) {
							if (currentIndustryName != null && currentIndustryName.indexOf("(") != -1 && currentIndustryName.substring(0, currentIndustryName.indexOf("(")).equals(content)) {
								continue;
							}
							currentIndustryName = content;
							if (currentIndustryName.indexOf(")") == -1) {
								if (j == table.getRows().size() - 1) {
									System.out.println("第" + i + "页 " + currentIndustryName + " 未完结");
								}
								currentIndustryName += table.getRows().get(j+1).getCells().get(k).getContent();
								
								previousIndustry = true;
								
								if(j == table.getRows().size() - 2 &&  table.getRows().get(j + 1).getCells().get(k).getContent().indexOf(")") == -1) {
									System.out.println("第" + i + "页 " + currentIndustryName + " 未完结");
								}
								
								if(j < table.getRows().size() - 2 && table.getRows().get(j + 1).getCells().get(k).getContent().indexOf(")") == -1){
									currentIndustryName += table.getRows().get(j+2).getCells().get(k).getContent();
								}
								
								if (!industryHashMap.containsKey(currentIndustryName)) {
									currentIndustryMap = new LinkedHashMap<String, Map<String, String>>();
									industryHashMap.put(currentIndustryName, currentIndustryMap);
									System.out.println(currentIndustryName);
								}
							}
						} else if (k == 0 && previousIndustry) {
							if(table.getRows().get(j).getCells().get(k).getContent().indexOf(")") != -1) {
								previousIndustry = false;
							}
						}

						if (k == 2) {
							if (!industriesIndex.contains(content)) {
								currentCompanyIndustry = new LinkedHashMap<String, String>();
								String industryName = table.getRows().get(j).getCells().get(k + 1).getContent();
								if(content.equals("19")) {
									int m =0;
									m=1;
								}
								if (table.getRows().get(j + 1).getCells().get(k).getContent().equals("") && !table.getRows().get(j + 1).getCells().get(k + 1).getContent().equals("")) {
									industryName += table.getRows().get(j + 1).getCells().get(k + 1).getContent();
								}
								currentIndustryMap.put(content + " " + industryName, currentCompanyIndustry);
								industriesIndex.add(content);
							}
						}

						if (k == 4) {
							currentCompanyIndustry.put(content, table.getRows().get(j).getCells().get(k + 1).getContent());
						}
					}
				}
			}
		}

		List<Industry> industries = new ArrayList<Industry>();
		for (String category : industryHashMap.keySet()) {
			Industry industry = new Industry();
			IndustryCompany[] industryCompanies = new IndustryCompany[industryHashMap.get(category).size()];
			int i = 0;
			for (String industryName : industryHashMap.get(category).keySet()) {
				Map<String, String> industryCompanyMap = industryHashMap.get(category).get(industryName);
				IndustryCompany industryCompany = new IndustryCompany();

				Company[] companies = new Company[industryCompanyMap.size()];
				int j = 0;
				for (String stockcd : industryCompanyMap.keySet()) {
					Company company = new Company();
					company.setCode(stockcd);
					company.setName(industryCompanyMap.get(stockcd));
					companies[j] = company;
					j++;
				}

				industryCompany.setName(industryName);
				industryCompany.setCompanies(companies);
				industryCompanies[i] = industryCompany;
				i++;
			}
			industry.setCategory(category);
			industry.setIndustries(industryCompanies);
			industries.add(industry);
		}

		JSONSerializer serializer = new JSONSerializer().prettyPrint(true);
		// System.out.println(serializer.exclude("*.class").deepSerialize(industries));

		for (int i = 0; i < industries.size(); i++) {
			if (industries.get(i).category.indexOf("(") != -1) {
				industries.get(i).category = industries.get(i).category.substring(0, industries.get(i).category.indexOf("("));
				for (int j = 0; j < industries.get(i).getIndustries().length; j++) {
					System.out.println(industries.get(i).getIndustries()[j].getName());
					String[] temp = industries.get(i).getIndustries()[j].getName().split(" ");
					if (temp.length > 1) {
						industries.get(i).getIndustries()[j].setName(temp[1]);
					}
				}
			}
		}

		System.out.println(serializer.exclude("*.class").deepSerialize(industries));
	}
}

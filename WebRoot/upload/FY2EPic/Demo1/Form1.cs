using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Net;
using System.IO;
using System.Threading;
using System.Text.RegularExpressions;

namespace Demo1
{
    public partial class Form1 : Form
    {
        public Form1()
        {
            InitializeComponent();
        }

        //获取网页源码
        public static string GetUrltoHtml(string a, string b)
        {

            try
            {
                WebRequest wReq = WebRequest.Create(a);
                // Get the response instance. 
                WebResponse wResp = wReq.GetResponse();
                Stream respStream = wResp.GetResponseStream();
                // Dim reader As StreamReader = New StreamReader(respStream) 
                using (StreamReader reader = new StreamReader(respStream, Encoding.GetEncoding(b)))
                {
                    return reader.ReadToEnd();
                }
            }
            catch (Exception ex)
            {

            }
            return "";
        }

        private Thread dataFetchThread = new Thread(FetchData);
        private bool isFetch = false;
        private static IFormatProvider ifp = new System.Globalization.CultureInfo("zh-CN", true);
        private void button1_Click(object sender, EventArgs e)
        {
            if (isFetch) return;


            if (dataFetchThread.IsAlive)
            {
                dataFetchThread.Start();
            }
            else
            {
                dataFetchThread = new Thread(FetchData);
                dataFetchThread.Start();
            }
            isFetch = true;
            label1.Text = "正在抓取...";
        }

        //停止抓取
        private void button3_Click(object sender, EventArgs e)
        {
            if (!isFetch) return;


            dataFetchThread.Abort();
            isFetch = false;
            label1.Text = "---";
        }

         //数据抓取实际代码
        private static void FetchData()
        {
            //URL，从网页源代码中提取的路径都是不带网址的，这个网址用来拼完整的代码
            string urlPrefix = @"http://image.nmc.cn/";


            //抓取的URL
            //List<string> urls = new List<string>(){ @"http://www.nmc.cn/publish/observations/china/dm/weatherchart-h000.htm",
            //                                        @"http://www.nmc.cn/publish/observations/china/dm/weatherchart-h850.htm",
            //                                        @"http://www.nmc.cn/publish/observations/hourly-winds.html"};
            Dictionary<string, string> urls = new Dictionary<string, string>();
            urls.Add("FY2E彩色云图", @"http://www.nmc.cn/publish/satellite/fy2.htm");
            //urls.Add("FY4A真彩色", @"http://www.nmc.cn/publish/satellite/FY4A-true-color.htm");
            //urls.Add("850hpa天气形势图",@"http://www.nmc.cn/publish/observations/china/dm/weatherchart-h850.htm");
            //urls.Add("地面风场图",@"http://www.nmc.cn/publish/observations/hourly-winds.html"); 

            //图片保存的路径
            string savePath = Path.Combine(Application.StartupPath, "pic");

            while (true)
            {
                //当前的日期
                string curDate = DateTime.Now.ToString("yyyyMMdd");
                string curDatePath = Path.Combine(savePath, curDate);       //当前日期对应的文件夹
                //if(!Directory.Exists(curDatePath))
                //    Directory.CreateDirectory(curDatePath);

                #region 将当天和前一天对应的文件夹中图片名称都读出来(用于判断图片是否下载过，下载过就不重复下载了)
                List<string> existPics = new List<string>();    //已经下载过的图片

                //前一天的日期
                string prevDate = DateTime.Now.AddDays(-1).ToString("yyyyMMdd");

                //前一天对应文件夹下的所有图片
                string prevDatePath = Path.Combine(savePath, prevDate);
                if (Directory.Exists(prevDatePath))
                {
                    string[] tmpPics = Directory.GetFiles(prevDatePath);
                    existPics.AddRange(tmpPics);
                }
                //当天对应文件夹下的所有图片
                if (Directory.Exists(curDatePath))
                {
                    string[] tmpPics = Directory.GetFiles(curDatePath);
                    existPics.AddRange(tmpPics);
                }
                #endregion

                //for (int i = 0; i < urls.Keys.Count; i++)
                foreach (string curKey in urls.Keys)
                {
                    #region 对列表中的URL进行抓取
                    string htmlVal = GetUrltoHtml(urls[curKey], "utf-8");    //html的源代码
                    
                    //使用正则表达式在URL中进行匹配
                    var picStrReg = new Regex(@"data.push\(\{.*\}\)");
                    MatchCollection matches = picStrReg.Matches(htmlVal);

                   
                    for (int j = 0; j < matches.Count; j++)
                    {
                        #region 从正则表达式的匹配结果中找到图片的路径的图片对应的时间
                        string picUrl;          //从Html中读取的图片路径
                        string picTime;         //图片对应的时间
                        string captureStr = matches[j].ToString();  //正则表达式匹配到的结果字符串
                        int tmpIdx = captureStr.IndexOf("img_path:'");
                        if (tmpIdx == -1) continue;
                        else
                        {
                            captureStr = captureStr.Substring(tmpIdx + 10);
                            tmpIdx = captureStr.IndexOf('\'');
                            if (tmpIdx == -1) continue;
                            else
                            {
                                picUrl = captureStr.Substring(0, tmpIdx);
                                tmpIdx = captureStr.IndexOf("ymd:'");
                                if (tmpIdx == -1) continue;
                                else
                                {
                                    captureStr = captureStr.Substring(tmpIdx + 5);
                                     tmpIdx = captureStr.IndexOf('\'');
                                     if (tmpIdx == -1) continue;
                                     else
                                     {
                                         picTime = captureStr.Substring(0, captureStr.IndexOf('\''));
                                     }
                                }                               
                            }
                        }

                        if (picUrl == "" || picTime == "") continue; 
                        #endregion

                        //将图片对应的时间从字符串转为DateTime格式
                        DateTime picTimeD = DateTime.MinValue;
                        if (!DateTime.TryParseExact(picTime, "yyyyMMdd HH:mm", ifp, System.Globalization.DateTimeStyles.None, out picTimeD))
                            continue;
                        //应该保存到的文件夹
                        string toSavePath = Path.Combine(savePath, picTimeD.ToString("yyyyMMdd")); 

                        //按URL读取JPG
                        string picName = picUrl.Substring(picUrl.LastIndexOf('/') + 1);
                        if (picName.Contains('?'))
                            picName = picName.Substring(0, picName.IndexOf('?'));
                        //JPG保存的路径和文件名
                        string picSavePath = picTimeD.ToString("yyyyMMddHHmm") +"_" + curKey + "_" + picName;
                        picSavePath = Path.Combine(toSavePath, picSavePath);
                        if (existPics.Contains(picSavePath)) continue;

                        if (!Directory.Exists(toSavePath))
                            Directory.CreateDirectory(toSavePath); 
                        //下载JPG
                        if (!Get_img(urlPrefix+picUrl, picSavePath)) continue;
                        Change_debug_Log_r("下载图片：" + picSavePath);
                    }
                    #endregion
                }

                Change_debug_Log_r("开始挂起，30min后下次运行");
                Thread.Sleep(1000 * 60 * 30);
                Change_debug_Log_r("结束挂起");
            }
        }

        private void button2_Click(object sender, EventArgs e)
        {
            DateTime date1 = DateTime.Now;

        }

        //记录日志
        private static void Change_debug_Log_r(string data)
        {
            string log_debug_path = Application.StartupPath + "\\log\\Log.txt";

            if (File.Exists(log_debug_path))
            {
                FileInfo fi = new FileInfo(log_debug_path);
                if (fi.Length / 1024 / 1024 > 5)
                {
                    try
                    {
                        File.Move(log_debug_path, log_debug_path.Replace("Log.txt", "Log_" + DateTime.Now.ToString("yyyy_MM_dd_HH_mm") + ".txt"));
                        //File.Create(log_debug_path);
                    }
                    catch { }
                }
            }


            if (File.Exists(log_debug_path))
            {

                FileStream fl = new FileStream(log_debug_path, FileMode.Append, FileAccess.Write);
                StreamWriter sr = new StreamWriter(fl);
                sr.WriteLine(DateTime.Now.ToString("yyyy/MM/dd HH:mm:ss") + ":" + data);
                sr.Close();
                fl.Close();
            }
            else
            {
                FileStream fl = new FileStream(log_debug_path, FileMode.Create, FileAccess.Write);
                StreamWriter sr = new StreamWriter(fl);
                sr.WriteLine("日志记录文件");
                sr.WriteLine(DateTime.Now.ToString("yyyy/MM/dd HH:mm:ss") + ":" + data);
                sr.Close();
                fl.Close();
            }
        }

        public static bool Get_img(string imgUrl,string savePath)
        {
            string[] file = imgUrl.Split('?');

            try
            {
                WebClient mywebclient = new WebClient();
                mywebclient.DownloadFile(imgUrl, savePath);
            }
            catch (Exception ex)
            {
                Change_debug_Log_r("下载图片失败：" + imgUrl + "  :  " + ex.Message);
                return false;
            }
            
            if(!File.Exists(savePath))
            {
                Change_debug_Log_r("下载图片失败：" + imgUrl );
                return false;
            }
            return true;
            //Bitmap img = null;
            //HttpWebRequest req;
            //HttpWebResponse res = null;
            //try
            //{
            //    System.Uri httpUrl = new System.Uri(imgpath);
            //    req = (HttpWebRequest)(WebRequest.Create(httpUrl));
            //    req.Timeout = 180000; //设置超时值10秒
            //    req.UserAgent = "XXXXX";
            //    req.Accept = "XXXXXX";
            //    req.Method = "GET";
            //    res = (HttpWebResponse)(req.GetResponse());
            //    img = new Bitmap(res.GetResponseStream());//获取图片流                
            //    img.Save(Path + @"\"+name);//随机名
            //}

            //catch (Exception ex)
            //{
            //    string aa = ex.Message;
            //}
            //finally
            //{
            //    res.Close();
            //}
        }
    }
}

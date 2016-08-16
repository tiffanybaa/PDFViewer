package the.pdfviewerx;

import org.ebookdroid.common.settings.AppSettings;
import org.ebookdroid.common.settings.books.BookSettings;
import org.ebookdroid.common.settings.books.Bookmark;
import org.ebookdroid.common.settings.types.BookRotationType;
import org.ebookdroid.common.settings.types.ToastPosition;
import org.ebookdroid.common.touch.MyTouchManagerView;
import org.ebookdroid.common.touch.TouchManagerView;
import org.ebookdroid.core.DecodeService;
import org.ebookdroid.core.codec.CodecFeatures;
import org.ebookdroid.core.models.DecodingProgressModel;
import org.ebookdroid.core.models.DocumentModel;
import org.ebookdroid.core.models.MySearchModel;
import org.ebookdroid.core.models.SearchModel;
import org.ebookdroid.core.models.ZoomModel;
import org.ebookdroid.ui.viewer.IActivityController;
import org.ebookdroid.ui.viewer.IView;
import org.ebookdroid.ui.viewer.IViewController;
import org.ebookdroid.ui.viewer.MYIActivityController;
import org.ebookdroid.ui.viewer.MyIView;
import org.ebookdroid.ui.viewer.MyViewerActivityController;
import org.ebookdroid.ui.viewer.ViewerActivityController;
import org.ebookdroid.ui.viewer.stubs.MyViewStub;
import org.ebookdroid.ui.viewer.stubs.ViewStub;
import org.ebookdroid.ui.viewer.viewers.GLView;
import org.ebookdroid.ui.viewer.viewers.MyGLView;
import org.ebookdroid.ui.viewer.views.ManualCropView;
import org.ebookdroid.ui.viewer.views.MyManualCropView;
import org.ebookdroid.ui.viewer.views.MySearchControls;
import org.ebookdroid.ui.viewer.views.PageViewZoomControls;
import org.ebookdroid.ui.viewer.views.SearchControls;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.Toast;

import org.emdev.common.android.AndroidVersion;
import org.emdev.ui.AbstractActionActivity;
import org.emdev.ui.actions.ActionDialogBuilder;
import org.emdev.ui.actions.ActionEx;
import org.emdev.ui.actions.ActionMenuHelper;
import org.emdev.ui.actions.IActionController;
import org.emdev.ui.actions.IActionParameter;
import org.emdev.ui.actions.MyActionDialogBuilder;
import org.emdev.ui.actions.MyActionEx;
import org.emdev.ui.actions.MyIActionController;
import org.emdev.ui.gl.GLConfiguration;
import org.emdev.ui.uimanager.IUIManager;
import org.emdev.utils.LayoutUtils;
import org.emdev.utils.LengthUtils;

import the.pdfviewerx.R;
import the.pdfviewerx.receivers.AdminReceiver;

public class MyViewerActivity extends Activity implements MYIActivityController {

    public static final DisplayMetrics DM = new DisplayMetrics();

    private MyViewerActivityController controller;
    
    public MyIView view;

    private Toast pageNumberToast;

    private Toast zoomToast;

    private PageViewZoomControls zoomControls;

    private MySearchControls searchControls;

    private FrameLayout frameLayout;

    private MyTouchManagerView touchView;

    private boolean menuClosedCalled;

    private MyManualCropView cropControls;
    
    private DevicePolicyManager policyManager;
	private ComponentName componentName;
	private static final int MY_REQUEST_CODE = 9999;

	
	private HomeKeyLocker locker = new HomeKeyLocker();
	
    /**
     * Instantiates a new base viewer activity.
     */
    public MyViewerActivity() {
        super();
    }

    public Context getContext() {
        return EBookDroidApp.context;
    }
   
    
    private void activeManage() {
		// TODO Auto-generated method stub
		
		Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);

		intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);


		intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
				"none");

		startActivityForResult(intent, MY_REQUEST_CODE);

	}
/*
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == MY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			policyManager.lockNow();
			finish();
		} else {
			activeManage();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
*/
    @Override
    public void onBackPressed() {
    	boolean x = true;
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.emdev.ui.AbstractActionActivity#onCreateImpl(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	
    	super.onCreate(savedInstanceState);
       
    	
    	//requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        //requestWindowFeature(Window.FEATURE_NO_TITLE); getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        frameLayout = new FrameLayout(this);
        
        view = MyViewStub.STUB;
        /*
        policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		componentName = new ComponentName(this, AdminReceiver.class);

		if (policyManager.isAdminActive(componentName)) {
			policyManager.lockNow();
			finish();
		} else {
			activeManage();
		}
*/
        try {
            GLConfiguration.checkConfiguration();

            view = new MyGLView(this);
            frameLayout.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                  | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                  | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                  | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                  | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                  | View.SYSTEM_UI_FLAG_IMMERSIVE);
            this.registerForContextMenu(view.getView());

            LayoutUtils.fillInParent(frameLayout, view.getView());

            frameLayout.addView(view.getView());
            frameLayout.addView(getZoomControls());
            frameLayout.addView(getManualCropControls());
            frameLayout.addView(getSearchControls());
            frameLayout.addView(getTouchView());
            ((View) view).setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                  | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                  | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                  | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                  | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                  | View.SYSTEM_UI_FLAG_IMMERSIVE);
            this.getWindow().setFlags(
            		WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

            getWindow().getDecorView().setSystemUiVisibility(flags);
            
        } catch (final Throwable th) {
            final MyActionDialogBuilder builder = new MyActionDialogBuilder(this, getController());
            builder.setTitle(R.string.error_dlg_title);
            builder.setMessage(th.getMessage());
            builder.setPositiveButton(R.string.error_close, R.id.mainmenu_close);
            builder.show();
        }

        setContentView(frameLayout);
       // getActionBar().hide();
        
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
        //locker.lock(this);
    }

    public final MyViewerActivityController getController() {
        
		if (controller == null) {
            controller = new MyViewerActivityController(this);
        }
        return controller;
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return (keyCode == KeyEvent.KEYCODE_BACK ? true : super.onKeyDown(keyCode, event));
    }

    

    /**
     * {@inheritDoc}
     * 
     * @see android.app.Activity#onWindowFocusChanged(boolean)
     */
    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        if (hasFocus && this.view != null) {
            IUIManager.instance.setFullScreenMode(this, this.view.getView(), AppSettings.current().fullScreen);
        }
    }

    public MyTouchManagerView getTouchView() {
        if (touchView == null) {
            touchView = new MyTouchManagerView(getController());
        }
        return touchView;
    }

    public void currentPageChanged(final String pageText, final String bookTitle) {
        if (LengthUtils.isEmpty(pageText)) {
            return;
        }

        final AppSettings app = AppSettings.current();
        if (IUIManager.instance.isTitleVisible(this) && app.pageInTitle) {
            getWindow().setTitle("(" + pageText + ") " + bookTitle);
            return;
        }

        if (app.pageNumberToastPosition == ToastPosition.Invisible) {
            return;
        }
        if (pageNumberToast != null) {
            pageNumberToast.setText(pageText);
        } else {
            pageNumberToast = Toast.makeText(this, pageText, Toast.LENGTH_SHORT);
        }

        pageNumberToast.setGravity(app.pageNumberToastPosition.position, 0, 0);
        pageNumberToast.show();
    }

    public void zoomChanged(final float zoom) {
        if (getZoomControls().isShown()) {
            return;
        }

        final AppSettings app = AppSettings.current();

        if (app.zoomToastPosition == ToastPosition.Invisible) {
            return;
        }

        final String zoomText = String.format("%.2f", zoom) + "x";

        if (zoomToast != null) {
            zoomToast.setText(zoomText);
        } else {
            zoomToast = Toast.makeText(this, zoomText, Toast.LENGTH_SHORT);
        }

        zoomToast.setGravity(app.zoomToastPosition.position, 0, 0);
        zoomToast.show();
    }

    public PageViewZoomControls getZoomControls() {
        if (zoomControls == null) {
            zoomControls = new PageViewZoomControls(this, getController().getZoomModel());
            zoomControls.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
        }
        return zoomControls;
    }

    public MySearchControls getSearchControls() {
        if (searchControls == null) {
            searchControls = new MySearchControls(this);
        }
        return searchControls;
    }

    public MyManualCropView getManualCropControls() {
        if (cropControls == null) {
            cropControls = new MyManualCropView(getController());
        }
        return cropControls;
    }
    
    public void showKeyboard() {
    	EditText yourEditText= (EditText) findViewById(R.id.search_controls_edit);
    	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    	imm.showSoftInput(yourEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * {@inheritDoc}
     * 
     * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View,
     *      android.view.ContextMenu.ContextMenuInfo)
     */
    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
       /* menu.clear();
        menu.setHeaderTitle(R.string.app_name);
        menu.setHeaderIcon(R.drawable.application_icon);
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu_context, menu);
        updateMenuItems(menu);
        */
    }

    /**
     * {@inheritDoc}
     * 
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        menu.clear();

        final MenuInflater inflater = getMenuInflater();

        if (hasNormalMenu()) {
            inflater.inflate(R.menu.mainmenu, menu);
        } else {
            inflater.inflate(R.menu.mainmenu_context, menu);
        }

        return true;
    }

    protected boolean hasNormalMenu() {
        return AndroidVersion.lessThan4x || IUIManager.instance.isTabletUi(this) || AppSettings.current().showTitle;
    }

    /**
     * {@inheritDoc}
     * 
     * @see android.app.Activity#onMenuOpened(int, android.view.Menu)
     */
    @Override
    public boolean onMenuOpened(final int featureId, final Menu menu) {
        view.changeLayoutLock(true);
        IUIManager.instance.onMenuOpened(this);
        return super.onMenuOpened(featureId, menu);
    }


    protected void updateMenuItems(final Menu menu) {
        final AppSettings as = AppSettings.current();

        ActionMenuHelper.setMenuItemChecked(menu, as.fullScreen, R.id.mainmenu_fullscreen);

        if (!AndroidVersion.lessThan3x) {
            ActionMenuHelper.setMenuItemChecked(menu, as.showTitle, R.id.mainmenu_showtitle);
        } else {
            ActionMenuHelper.setMenuItemVisible(menu, false, R.id.mainmenu_showtitle);
        }

        ActionMenuHelper
                .setMenuItemChecked(menu, getZoomControls().getVisibility() == View.VISIBLE, R.id.mainmenu_zoom);

        final BookSettings bs = getController().getBookSettings();
        if (bs == null) {
            return;
        }

        ActionMenuHelper.setMenuItemChecked(menu, bs.rotation == BookRotationType.PORTRAIT,
                R.id.mainmenu_force_portrait);
        ActionMenuHelper.setMenuItemChecked(menu, bs.rotation == BookRotationType.LANDSCAPE,
                R.id.mainmenu_force_landscape);
        ActionMenuHelper.setMenuItemChecked(menu, bs.nightMode, R.id.mainmenu_nightmode);
        ActionMenuHelper.setMenuItemChecked(menu, bs.cropPages, R.id.mainmenu_croppages);
        ActionMenuHelper.setMenuItemChecked(menu, bs.splitPages, R.id.mainmenu_splitpages,
                R.drawable.viewer_menu_split_pages, R.drawable.viewer_menu_split_pages_off);

        final DecodeService ds = getController().getDecodeService();

        final boolean cropSupported = ds.isFeatureSupported(CodecFeatures.FEATURE_CROP_SUPPORT);
        ActionMenuHelper.setMenuItemVisible(menu, cropSupported, R.id.mainmenu_croppages);
        ActionMenuHelper.setMenuItemVisible(menu, cropSupported, R.id.mainmenu_crop);

        final boolean splitSupported = ds.isFeatureSupported(CodecFeatures.FEATURE_SPLIT_SUPPORT);
        ActionMenuHelper.setMenuItemVisible(menu, splitSupported, R.id.mainmenu_splitpages);

        final MenuItem navMenu = menu.findItem(R.id.mainmenu_nav_menu);
        if (navMenu != null) {
            final SubMenu subMenu = navMenu.getSubMenu();
            subMenu.removeGroup(R.id.actions_goToBookmarkGroup);
            if (AppSettings.current().showBookmarksInMenu && LengthUtils.isNotEmpty(bs.bookmarks)) {
                for (final Bookmark b : bs.bookmarks) {
                    addBookmarkMenuItem(subMenu, b);
                }
            }
        }

    }

    protected void addBookmarkMenuItem(final Menu menu, final Bookmark b) {
        final MenuItem bmi = menu.add(R.id.actions_goToBookmarkGroup, R.id.actions_goToBookmark, Menu.NONE, b.name);
        bmi.setIcon(R.drawable.viewer_menu_bookmark);
        ActionMenuHelper.setMenuItemExtra(bmi, "bookmark", b);
    }

    /**
     * {@inheritDoc}
     * 
     * @see android.app.Activity#onPanelClosed(int, android.view.Menu)
     */
    @Override
    public void onPanelClosed(final int featureId, final Menu menu) {
        menuClosedCalled = false;
        super.onPanelClosed(featureId, menu);
        if (!menuClosedCalled) {
            onOptionsMenuClosed(menu);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see android.app.Activity#onOptionsMenuClosed(android.view.Menu)
     */
    @Override
    public void onOptionsMenuClosed(final Menu menu) {
        menuClosedCalled = true;
        IUIManager.instance.onMenuClosed(this);
        view.changeLayoutLock(false);
    }

    /**
     * {@inheritDoc}
     * 
     * @see android.app.Activity#dispatchKeyEvent(android.view.KeyEvent)
     */
    @Override
    public final boolean dispatchKeyEvent(final KeyEvent event) {
        view.checkFullScreenMode();
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
            if (!hasNormalMenu()) {
                getController().getOrCreateAction(R.id.actions_openOptionsMenu).run();
                return true;
            }
        }

        if (getController().dispatchKeyEvent(event)) {
        	//locker.unlock();
            getActionBar().show();
           // locker.lock(this);
        	return true;
        }

        return super.dispatchKeyEvent(event);
    }

    public void showToastText(final int duration, final int resId, final Object... args) {
        Toast.makeText(getApplicationContext(), getResources().getString(resId, args), duration).show();
    }

	@Override
	public MyIActionController<?> getMyParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MyViewerActivity getManagedComponent() {
		return this;
	}

	@Override
	public void setManagedComponent(MyViewerActivity component) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public MyActionEx getAction(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MyActionEx getOrCreateAction(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MyActionEx createAction(int id, IActionParameter... parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Activity getActivity() {
		return this;
	}

	@Override
	public BookSettings getBookSettings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DecodeService getDecodeService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DocumentModel getDocumentModel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MySearchModel getSearchModel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MyIView getView() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IViewController getDocumentController() {
		return getController().getDocumentController();
	}

	@Override
	public MyIActionController<?> getActionController() {
		return getController();
	}

	@Override
	public ZoomModel getZoomModel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DecodingProgressModel getDecodingProgressModel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void jumpToPage(int viewIndex, float offsetX, float offsetY, boolean addToHistory) {
		// TODO Auto-generated method stub
		
	}

	

}